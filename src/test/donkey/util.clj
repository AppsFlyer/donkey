(ns donkey.util
  (:require [clojure.test :refer [is]]
            [donkey.core :as donkey]
            [donkey.server :as server])
  (:import (io.vertx.ext.web.client WebClient WebClientOptions HttpResponse)
           (io.vertx.core Vertx Future Handler)
           (com.appsflyer.donkey.server Server)
           (donkey.server DonkeyServer)))

(def ^:dynamic donkey-server)
(def ^:dynamic ^WebClient client)

(def ^:const
  default-options {:port 16969 :event-loops 1 :worker-threads 4 :metrics-enabled true})

(defn- launch-server [opts]
  (let [instance (donkey/create-server (merge default-options opts))]
    (server/startSync instance)
    instance))

(defn- ^WebClient launch-client [^Vertx vertx]
  (WebClient/create
    vertx
    (-> (WebClientOptions.)
        (.setDefaultHost "localhost")
        (.setDefaultPort (int (:port default-options))))))

(defn- get-vertx-instance [^DonkeyServer donkey-server]
  (.vertx ^Server (.-impl donkey-server)))

(defn init
  "Initializes the server and client instances that are used in the test"
  ([test-fn routes] (init test-fn routes nil))
  ([test-fn routes middleware]
   (binding [donkey-server (launch-server {:routes routes :middleware middleware})]
     (binding [client (launch-client (get-vertx-instance donkey-server))]
       (test-fn)
       (.close client)
       (is (nil? (server/stopSync donkey-server)))))))


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


