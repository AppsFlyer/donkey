(ns com.appsflyer.donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.middleware.params :as middleware]
            [com.appsflyer.donkey.server-spec :as server-spec])
  (:import (com.appsflyer.donkey.server Server DonkeyServer)))

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

;;todo consider putting the spec definitions in this namespace
(defn ^DonkeyServer create-server [opts]
  (-> (spec/assert ::server-spec/config opts)
      server/get-server-config
      Server/create
      server/->DonkeyServer))

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

(defn new-server []
  (create-server
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
                            :handler print-query-params-and-headers}]
     :metrics-enabled     false}))
