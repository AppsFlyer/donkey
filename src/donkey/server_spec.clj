(ns donkey.server-spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string])
  (:import (com.codahale.metrics MetricRegistry)))


;; ------- Route Specification ------- ;;

(s/def ::path string?)
(s/def ::methods (s/coll-of keyword?))
(s/def ::consumes (s/coll-of string?))
(s/def ::produces (s/coll-of string?))
(s/def ::handler-mode #{:blocking :non-blocking})
(s/def ::match-type #{:simple :regex})
(s/def ::handler fn?)

(s/def ::route (s/keys :req-un [::handler]
                       :opt-un [::path
                                ::methods
                                ::consumes
                                ::produces
                                ::handler-mode
                                ::match-type]))

;; ------- Server Configuration Specification ------- ;;

(s/def ::port #(s/int-in-range? 1 65536 %))
(s/def ::compression boolean?)
(s/def ::host (s/and string? (comp not clojure.string/blank?)))
(s/def ::metrics-enabled boolean?)
(s/def ::metrics-registry #(instance? MetricRegistry %))
(s/def ::metrics-prefix string?)
(s/def ::worker-threads #(s/int-in-range? 1 500 %))
(s/def ::debug boolean?)
(s/def ::idle-timeout-seconds (s/and int? #(or (zero? %) (pos? %))))
(s/def ::routes (s/coll-of ::route :distinct true :min-count 1))
(s/def ::ssl boolean?)
(s/def ::ssl-type #{:jks :pem})
(s/def ::key-store-path string?)
(s/def ::key-store-password string?)
(s/def ::pem-key-path string?)
(s/def ::pem-cert-path string?)
(s/def ::jmx-enabled boolean?)
(s/def ::jmx-domain string?)

(s/def ::config (s/keys :req-un [::port ::routes]
                        :opt-un [::compression
                                 ::host
                                 ::metrics-enabled
                                 ::metrics-registry
                                 ::metrics-prefix
                                 ::worker-threads
                                 ::debug
                                 ::idle-timeout-seconds
                                 ::ssl
                                 ::ssl-type
                                 ::key-store-path
                                 ::key-store-password
                                 ::pem-key-path
                                 ::pem-cert-path
                                 ::jmx-enabled
                                 ::jmx-domain]))
(comment
  (let [config {:port                 8080
                :compression          false
                :host                 "0.0.0.0"
                :metrics-enabled      true
                :metrics-prefix       "donkey"
                :worker-threads       20
                :debug                false
                :idle-timeout-seconds 0
                :routes               [{:handler identity}]
                :ssl                  false
                :ssl-type             :jks
                :key-store-path       "/mykeystore.jks"
                :key-store-password   "foo"
                :pem-key-path         "/my-key.pem"
                :pem-cert-path        "/my-cert.pem"
                :jmx-enabled          false
                :jmx-domain           "localhost"}]
    (s/explain ::config config)
    #_(s/conform ::config config))
  )
