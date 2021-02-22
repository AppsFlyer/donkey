;
; Copyright 2020-2021 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
;

(ns com.appsflyer.donkey.test-helper
  (:require [clojure.test :refer [is]]
            [com.appsflyer.donkey.core :as donkey]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.request :as request]
            [com.appsflyer.donkey.client :as client])
  (:import (io.vertx.ext.web.client WebClient WebClientOptions HttpResponse)
           (io.vertx.core Vertx Future Handler)
           (com.appsflyer.donkey.server DonkeyServer)
           (com.appsflyer.donkey.core Donkey)
           (com.appsflyer.donkey.client DonkeyClient)
           (com.appsflyer.donkey FutureResult)))

(def ^:dynamic ^Donkey donkey-core)
(def ^:dynamic ^DonkeyServer donkey-server)
(def ^:dynamic ^DonkeyClient donkey-client)
(def ^:dynamic ^WebClient vertx-client)

(def ^:const DEFAULT-HOST "localhost")
(def ^:const DEFAULT-PORT 16969)

(def ^:const
  default-server-options {:port DEFAULT-PORT})

(def ^:const
  default-client-options {:default-host DEFAULT-HOST :default-port DEFAULT-PORT})

(def ^:const
  default-donkey-options {:instances 4 :event-loops 2 :worker-threads 4})

(defn- ^WebClient launch-vertx-client [^Vertx vertx]
  (WebClient/create
    vertx
    (-> (WebClientOptions.)
        (.setDefaultHost DEFAULT-HOST)
        (.setDefaultPort (int (:port default-server-options))))))

(defn init-web-client [test-fn]
  (binding [vertx-client (launch-vertx-client (-> donkey-core .-config :vertx))]
    (test-fn)
    (.close vertx-client)))

(defn init-donkey-client [test-fn]
  (binding [donkey-client (donkey/create-client donkey-core default-client-options)]
    (test-fn)
    (client/stop donkey-client)))

(defn- launch-donkey-server [^Donkey donkey-instance opts]
  (let [instance (donkey/create-server donkey-instance (merge default-server-options opts))]
    (server/start-sync instance)
    instance))

(defn init-donkey-server
  ([test-fn routes] (init-donkey-server test-fn routes nil))
  ([test-fn routes resources] (init-donkey-server test-fn routes resources []))
  ([test-fn routes resources middleware]
   (binding [donkey-server (launch-donkey-server
                             donkey-core
                             (into {} (remove #(nil? (second %))
                                              {:routes     routes
                                               :middleware middleware
                                               :resources  resources})))]
     (test-fn)
     (is (nil? (server/stop-sync donkey-server))))))


(defn init-donkey [test-fn]
  (binding [donkey-core (donkey/create-donkey default-donkey-options)]
    (test-fn)))

(defn run-with-server-and-client
  "Run `test-fn` under the context of a new DonkeyServer and Vertx WebClient.
  Creates a server instance according to the given `routes` and optional `middleware`,
  and a default client. Both will be closed after the `test-fn` returns.
  The server and client are available inside the test as
  `donkey-server` and `vertx-client` respectively."
  ([test-fn routes] (run-with-server-and-client test-fn routes []))
  ([test-fn routes middleware]
   (let [^Donkey donkey-instance (donkey/create-donkey default-donkey-options)]
     (binding [donkey-server (launch-donkey-server donkey-instance {:routes routes :middleware middleware})
               vertx-client (launch-vertx-client (-> donkey-instance .-config :vertx))]
       (test-fn)
       (.close vertx-client)
       (is (nil? (server/stop-sync donkey-server)))))))


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
  (reify Handler
    (handle [_this res]
      (deliver response-promise ^Future res))))

(defn make-pre-processing-middleware [fun]
  (fn [handler]
    (fn
      ([req]
       (handler (fun req)))
      ([req respond raise]
       (fun req (fn [res] (respond (handler res respond raise))) raise)))))

(defn make-post-processing-middleware [fun]
  (fn [handler]
    (fn
      ([req]
       (fun (handler req)))
      ([req respond raise]
       (handler req (fn [res] (respond (fun res respond raise))) raise)))))

(defn ^FutureResult make-request
  ([opts]
   (->
     (client/request donkey-client opts)
     request/submit))
  ([opts body]
   (->
     (client/request donkey-client opts)
     (request/submit body))))

(defn ^FutureResult submit-form [opts body]
  (->
    (client/request donkey-client opts)
    (request/submit-form body)))

(defn ^FutureResult submit-multi-part-form [opts body]
  (->
    (client/request donkey-client opts)
    (request/submit-multipart-form body)))


