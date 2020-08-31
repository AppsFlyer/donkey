(ns com.appsflyer.donkey.donkey-spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string])
  (:import (com.codahale.metrics MetricRegistry)))


;; ------- Donkey Specification ------- ;;

(s/def ::metrics-enabled boolean?)
(s/def ::metrics-prefix string?)
(s/def ::metric-registry #(instance? MetricRegistry %))
(s/def ::instances #(s/int-in-range? 1 500 %))
(s/def ::worker-threads #(s/int-in-range? 1 500 %))
(s/def ::event-loops #(s/int-in-range? 1 (.availableProcessors (Runtime/getRuntime)) %))

(s/def ::donkey-config (s/keys :req-un [::instances]
                               :opt-un [::metrics-prefix
                                        ::metric-registry
                                        ::metrics-enabled
                                        ::worker-threads
                                        ::event-loops]))


(s/def ::pos-int (s/and int? #(pos? %)))
(s/def ::handler fn?)
(s/def ::handlers (s/coll-of ::handler))
(s/def ::strings (s/coll-of string?))


;; ------- Middleware Specification ------- ;;

(s/def ::middleware ::handlers)


;; ------- Route Specification ------- ;;

(s/def ::path string?)
(s/def ::methods (s/coll-of keyword?))
(s/def ::consumes ::strings)
(s/def ::produces ::strings)
(s/def ::handler-mode #{:blocking :non-blocking})
(s/def ::match-type #{:simple :regex})

(s/def ::route (s/keys :req-un [::handler]
                       :opt-un [::path
                                ::methods
                                ::consumes
                                ::produces
                                ::handler-mode
                                ::match-type
                                ::middleware]))


;; ------- Server Specification ------- ;;

(s/def ::port #(s/int-in-range? 1 65536 %))
(s/def ::compression boolean?)
(s/def ::host (s/and string? (comp not clojure.string/blank?)))
(s/def ::debug boolean?)
(s/def ::date-header boolean?)
(s/def ::content-type-header boolean?)
(s/def ::server-header boolean?)
(s/def ::idle-timeout-seconds (s/and int? #(or (zero? %) (pos? %))))
(s/def ::routes (s/coll-of ::route :distinct true :min-count 1))

(s/def ::server-config (s/keys :req-un [::port]
                               :opt-un [::middleware
                                        ::compression
                                        ::host
                                        ::debug
                                        ::date-header
                                        ::content-type-header
                                        ::server-header
                                        ::idle-timeout-seconds]))

(comment
  (let [config {:port                 8080
                :compression          false
                :host                 "localhost"
                :metrics-enabled      true
                :metrics-prefix       "donkey"
                :worker-threads       20
                :debug                false
                ::date-header         false
                ::content-type-header false
                ::server-header       false
                :idle-timeout-seconds 0
                :routes               [{:middleware [identity]
                                        :handler    [identity]}]}]))


;; ------- Client Specification ------- ;;

(s/def ::keep-alive boolean?)
(s/def ::keep-alive-timeout-seconds ::pos-int)
(s/def ::connect-timeout-seconds ::pos-int)
(s/def ::max-redirects ::pos-int)
(s/def ::default-port ::port)
(s/def ::default-host ::host)
(s/def ::proxy-type #{:http :sock4 :sock5})
(s/def ::proxy (s/keys :req-un [::host ::port ::proxy-type]))

(s/def ::client-config (s/keys :opt-un [::middleware
                                        ::compression
                                        ::default-host
                                        ::default-port
                                        ::debug
                                        ::keep-alive
                                        ::keep-alive-timeout-seconds
                                        ::connect-timeout-seconds
                                        ::max-redirects
                                        ::idle-timeout-seconds
                                        ::proxy]))

(comment
  (let [config {:keep-alive                 false
                :keep-alive-timeout-seconds 60
                :debug                      false
                :idle-timeout-seconds       0
                :connect-timeout-seconds    60
                :default-port               80
                :default-host               "localhost"
                :max-redirects              16
                :proxy-type                 {:host "localhost"
                                             :port 3128
                                             :type :http|:sock4|:sock5}
                :compression                false}])

  )
