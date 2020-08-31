(ns com.appsflyer.donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.metrics :as metrics]
            [com.appsflyer.donkey.middleware.params :as middleware]
            [com.appsflyer.donkey.donkey-spec :as donkey-spec])
  (:import (com.appsflyer.donkey.server Server DonkeyServer)
           (io.vertx.core Vertx VertxOptions)))

(comment
  ;;; Server API
  {:port                 8080
   :compression          false
   :host                 "0.0.0.0"
   :metrics-enabled      true
   :metric-registry      nil
   :metrics-prefix       "donkey"
   :worker-threads       20
   :event-loops          1
   :debug                false
   :idle-timeout-seconds 0
   :middleware           []
   :routes               []}

  ;;; Route API
  {:methods      [:get :post]
   :consumes     ["application/json" "application/x-www-form-urlencoded" "text/plain"]
   :produces     ["application/json" "text/plain"]
   :handler-mode :non-blocking
   :handler      (fn [req respond raise] (respond {:status 200}))
   :middleware   [(fn [handler] (fn [req respond raise]
                                  (-> req handler identity respond)))]
   :path         "/foo"
   :match-type   :simple}

  )

(defprotocol IDonkey
  (create-server [_this opts])
  (create-client [_this opts]))

(deftype Donkey [^Vertx vertx]
  IDonkey
  (create-server [_this opts]
    (-> (spec/assert ::donkey-spec/server-config opts)
        (assoc :vertx vertx)
        server/get-server-config
        Server/create
        server/->DonkeyServer))
  (create-client [_this opts]
    ))

(defn- ^VertxOptions get-vertx-options
  "Creates and returns a VertxOptions object from the opts map.
  The vertx options are used to initialise the Vertx object which is an
  integral part of the server. It allows configuring thread pools,
  clustering, and metrics."
  [opts]
  (let [vertx-options (VertxOptions.)]
    (.setPreferNativeTransport vertx-options true)
    (.setEventLoopPoolSize vertx-options (int (:event-loops opts 1)))
    (when-let [worker-threads (:worker-threads opts)]
      (.setWorkerPoolSize vertx-options (int worker-threads)))
    (when (:metrics-enabled opts)
      (.setMetricsOptions vertx-options (metrics/get-metrics-options opts)))
    vertx-options))

(defn- print-query-params-and-headers [req respond _raise]
  (respond
    {:body (format "Query parameters: %s. Headers: %s"
                   (apply str (seq (:query-params req)))
                   (apply str (seq (:headers req))))}))

(defn- add-headers [handler]
  (fn [req respond raise]
    (handler
      (update req :headers assoc "DNT" 1 "Cache-Control" "no-cache")
      respond
      raise)))

(defn ^Donkey create-donkey [opts]
  (-> (spec/assert ::donkey-spec/donkey-config opts)
      get-vertx-options
      Vertx/vertx
      ->Donkey))

(defn ^DonkeyServer new-server [^Donkey donkey]
  (create-server
    donkey
    {:port                8080
     :middleware          [middleware/keywordize-query-params add-headers]
     :debug               false
     :date-header         true
     :content-type-header true
     :server-header       true
     :routes              [{:path     "/plaintext"
                            :methods  [:get]
                            :produces ["text/plain"]
                            :handler  (fn [_req res _err] (res {:body "Hello, world!"}))}
                           {:path    "/benchmark"
                            :methods [:get]
                            :handler print-query-params-and-headers}]}))
