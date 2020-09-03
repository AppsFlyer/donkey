(ns com.appsflyer.donkey.util
  (:require [clojure.test :refer [is]]
            [com.appsflyer.donkey.core :as donkey]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.client :as client])
  (:import (io.vertx.ext.web.client WebClient WebClientOptions HttpResponse)
           (io.vertx.core Vertx Future Handler)
           (com.appsflyer.donkey.server DonkeyServer)
           (com.appsflyer.donkey.core Donkey)
           (com.appsflyer.donkey.client DonkeyClient)))

(def ^:dynamic ^Donkey donkey-core)
(def ^:dynamic ^DonkeyServer donkey-server)
(def ^:dynamic ^DonkeyClient donkey-client)
(def ^:dynamic ^WebClient vertx-client)

(def ^:const DEFAULT-HOST "localhost")
(def ^:const DEFAULT-PORT 16969)

(def ^:const
  default-server-options {:port DEFAULT-PORT :metrics-enabled true})

(def ^:const
  default-client-options {:default-host DEFAULT-HOST :default-port DEFAULT-PORT})

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
        (.setDefaultHost DEFAULT-HOST)
        (.setDefaultPort (int (:port default-server-options))))))

(defn init-donkey [test-fn]
  (binding [donkey-core (donkey/create-donkey default-donkey-options)]
    (test-fn)))

(defn init-donkey-server
  ([test-fn routes] (init-donkey-server test-fn routes nil))
  ([test-fn routes middleware]
   (binding [donkey-server (launch-server donkey-core {:routes routes :middleware middleware})]
     (test-fn)
     (is (nil? (server/stop-sync donkey-server))))))

(defn init-donkey-client [test-fn]
  (binding [donkey-client (donkey/create-client donkey-core default-client-options)]
    (test-fn)
    (client/stop donkey-client)))

(defn init-web-client [test-fn]
  (binding [vertx-client (launch-client (.-vertx donkey-core))]
    (test-fn)
    (.close vertx-client)))

(defn init
  "Initializes the server and client instances that are used in the test"
  ([test-fn routes] (init test-fn routes nil))
  ([test-fn routes middleware]
   (let [^Donkey donkey-instance (donkey/create-donkey default-donkey-options)]
     (binding [donkey-server (launch-server donkey-instance {:routes routes :middleware middleware})]
       (binding [vertx-client (launch-client (.-vertx donkey-instance))]
         (test-fn)
         (.close vertx-client)
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


