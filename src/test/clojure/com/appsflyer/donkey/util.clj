(ns com.appsflyer.donkey.util
  (:require [clojure.test :refer [is]]
            [com.appsflyer.donkey.core :as donkey]
            [com.appsflyer.donkey.server :as server])
  (:import (io.vertx.ext.web.client WebClient WebClientOptions HttpResponse)
           (io.vertx.core Vertx Future Handler)
           (com.appsflyer.donkey.server DonkeyServer Server)
           (com.appsflyer.donkey.core Donkey)))

(def ^:dynamic donkey-server)
(def ^:dynamic ^WebClient client)

(def ^:const
  default-server-options {:port 16969 :metrics-enabled true})

(def ^:const
  default-donkey-options {:instances 4 :event-loops 1 :worker-threads 4})

(defn- launch-server [^Donkey donkey-instance opts]
  (let [instance (donkey/create-server donkey-instance (merge default-server-options opts))]
    (server/start-sync instance)
    instance))

(defn- ^WebClient launch-client [^Vertx vertx]
  (WebClient/create
    vertx
    (-> (WebClientOptions.)
        (.setDefaultHost "localhost")
        (.setDefaultPort (int (:port default-server-options))))))

(defn- get-vertx-instance [^DonkeyServer donkey-server]
  (.vertx ^Server (.-impl donkey-server)))

(defn init
  "Initializes the server and client instances that are used in the test"
  ([test-fn routes] (init test-fn routes nil))
  ([test-fn routes middleware]
   (let [^Donkey donkey-instance (donkey/create-donkey default-donkey-options)]
     (binding [donkey-server (launch-server donkey-instance {:routes routes :middleware middleware})]
       (binding [client (launch-client (.-vertx donkey-instance))]
         (test-fn)
         (.close client)
         (is (nil? (server/stop-sync donkey-server))))))))


;; ---------- Helper Functions ---------- ;;


(defn ^HttpResponse wait-for-response
  "Waits (blocks) until the `response-promise` is resolved.
  Returns the result on success, or throws the exception if failed."
  [response-promise]
  (let [^Future future-result @response-promise]
    (when (.failed future-result)
      (throw (.cause future-result)))
    (.result future-result)))

(defn parse-response-body [^HttpResponse res]
  (-> res .bodyAsString read-string))

(defn parse-response-body-when-resolved [response-promise]
  (-> response-promise wait-for-response parse-response-body))

(defn ^Handler create-client-handler
  "Create a handler that resolves `response-promise` when the client receives a response"
  [response-promise]
  (server/->EventHandler (fn [^Future res] (deliver response-promise res))))


