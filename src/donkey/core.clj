(ns donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [donkey.server :as server]
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
       :middleware      [{:handler-mode :non-blocking
                          :handler      (fn [req respond _raise]
                                          (respond (update req :query-params clojure.walk/keywordize-keys)))}]
       :routes          [{:path     "/greet/:name"
                          :methods  [:get]
                          :produces ["application/json"]
                          :handlers [(fn [req respond _raise]
                                       (future
                                         (respond
                                           {:body (.getBytes
                                                    (str "{\"greet\":\"Hello " (-> :path-params req (get "name")) "\"}"))})))]}
                         {:path         "/foo"
                          :methods      [:get]
                          :handler-mode :blocking
                          :handlers     [(fn [req]
                                           (println req)
                                           {:body (str (:query-params req))})]}]
       :metrics-enabled true
       :metrics-prefix  "donkey"}
      create-server))
