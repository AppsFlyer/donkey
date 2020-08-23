(ns donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [donkey.server :as server]
            [donkey.middleware.params :as middleware]
            [donkey.server-spec :as server-spec])
  (:import (donkey.server DonkeyServer)
           (com.appsflyer.donkey.server Server)))

(comment
  ;;; Server API
  {:port                 8080
   :compression          false
   :host                 "0.0.0.0"
   :metrics-enabled      true
   :metrics-registry     nil
   :metrics-prefix       "donkey"
   :worker-threads       20
   :event-loops          1
   :debug                false
   :idle-timeout-seconds 0
   :middleware           {}
   :routes               []}

  ;;; Route API
  {:methods      [:get :post]
   :consumes     ["application/json" "application/x-www-form-urlencoded" "text/plain"]
   :produces     ["application/json" "text/plain"]
   :handler-mode :non-blocking
   :handler      (fn [req respond raise] (respond {:status 200}))
   :middleware   {:handlers     [(fn [handler] (fn [req respond raise]
                                                 (-> req handler identity respond)))]
                  :handler-mode :non-blocking}
   :path         "/foo"
   :match-type   :simple}

  )

;;todo consider putting the spec definitions in this namespace
(defn ^DonkeyServer create-server [opts]
  (-> (spec/assert ::server-spec/config opts)
      server/get-server-config
      Server.
      server/->DonkeyServer))

(defn- print-query-params-and-headers [req respond _raise]
  (respond
    {:body (format "Query parameters: %s. Headers: %s"
                   (apply str (seq (:query-params req)))
                   (apply str (seq (:headers req))))}))

(defn- add-headers [handler]
  (fn [req respond _raise]
    (-> req
        (update :headers assoc "DNT" 1 "Cache-Control" "no-cache")
        handler
        respond)))

(defn new-server []
  (-> {:port            8080
       :event-loops     16
       :middleware      {:handlers [middleware/keywordize-query-params
                                    add-headers]}
       :routes          [{:path    "/benchmark"
                          :methods [:get]
                          :handler print-query-params-and-headers}]
       :metrics-enabled false}
      create-server))
