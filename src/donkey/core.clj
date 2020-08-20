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
   :middleware           []
   :routes               []}

  ;;; Route API
  {:methods      [:get :post]
   :consumes     ["application/json" "application/x-www-form-urlencoded" "text/plain"]
   :produces     ["application/json" "text/plain"]
   :handler-mode :non-blocking
   :handlers     [(fn [req respond raise]
                    (respond {:status 200}))]
   :path         "/foo"
   :match-type   :simple}

  )

;;todo consider putting the spec definitions in this namespace
(defn ^DonkeyServer create-server [opts]
  (-> (spec/assert ::server-spec/config opts)
      server/get-server-config
      Server.
      server/->DonkeyServer))


(defn new-server []
  (-> {:port            8080
       :event-loops     16
       :middleware      [{:handler middleware/keywordize-query-params}
                         {:handler (fn [req respond _raise]
                                     (respond (update req :headers assoc "DNT" 1 "Cache-Control" "no-cache")))}]
       :routes          [{:path     "/benchmark"
                          :methods  [:get]
                          :handlers [(fn [req respond _raise]
                                       (respond
                                         {:body (format "Query parameters: %s. Headers: %s"
                                                        (apply str (seq (:query-params req)))
                                                        (apply str (seq (:headers req))))}))]}]
       :metrics-enabled false}
      create-server))
