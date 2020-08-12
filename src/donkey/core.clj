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
   :routes               []
   :ssl                  false
   :ssl-type             :jks | :pem
   :key-store-path       "/mykeystore.jks"
   :key-store-password   "foo"
   :pem-key-path         "/my-key.pem"
   :pem-cert-path        "/my-cert.pem"
   :jmx-enabled          false
   :jmx-domain           "localhost"}

  ;;; Route API
  {:methods       [:get :post]
   :consumes      ["application/json" "application/x-www-form-urlencoded" "text/plain"]
   :produces      ["application/json" "text/plain"]
   :handler-mode :non-blocking
   :handler      (fn [req respond raise]
                   (respond {:status 200}))
   :path         "/foo"
   :match-type   :simple}

  )

;;todo consider assert instead of conform.
;;todo consider putting the spec definitions in this namespace
(defn ^DonkeyServer create-server [opts]
  (let [normalized-opts (spec/conform ::server-spec/config opts)]
    (if (= normalized-opts ::spec/invalid)
      (throw (ex-info "Invalid argument" (spec/explain-data ::server-spec/config opts)))
      (server/->DonkeyServer
        (Server. (server/get-server-config opts))))))



(defn new-server []
  (-> {:port   8080
       :routes [{:path            "/greet/:name"
                 :methods         [:get]
                 :metrics-enabled true
                 :consumes        ["text/plain"]
                 :handler         (fn [req respond _raise]
                                    (future
                                      (respond
                                        {:status  200
                                         :headers {"content-type" "text/plain"}
                                         :body    (.getBytes
                                                    (str "Hello " (-> :path-params req (get "name"))))})))}]}
      create-server))

