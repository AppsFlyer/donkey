(ns com.appsflyer.donkey.donkey-spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string])
  (:import (com.codahale.metrics MetricRegistry)
           (io.vertx.core.impl.cpu CpuCoreSensor)
           (java.io InputStream)))


;; ------- Donkey Specification ------- ;;

(s/def ::metrics-enabled boolean?)
(s/def ::metrics-prefix string?)
(s/def ::metric-registry #(instance? MetricRegistry %))
(s/def ::worker-threads #(s/int-in-range? 1 500 %))
(s/def ::event-loops #(s/int-in-range? 1 (CpuCoreSensor/availableProcessors) %))

(s/def ::donkey-config (s/keys :opt-un [::metrics-prefix
                                        ::metric-registry
                                        ::metrics-enabled
                                        ::worker-threads
                                        ::event-loops]))


(s/def ::pos-int (s/and int? #(pos? %)))
(s/def ::handler fn?)
(s/def ::handlers (s/coll-of ::handler))
(s/def ::strings (s/coll-of string?))
(s/def ::not-blank (s/and string? (comp not clojure.string/blank?)))
(s/def ::method #{:get :post :put :delete :options :head :trace :connect :patch})



;; ------- Middleware Specification ------- ;;

(s/def ::middleware ::handlers)


;; ------- Route Specification ------- ;;

(s/def ::path string?)
(s/def ::methods (s/coll-of ::method))
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

(s/def ::instances #(s/int-in-range? 1 500 %))
(s/def ::port #(s/int-in-range? 1 65536 %))
(s/def ::compression boolean?)
(s/def ::host ::not-blank)
(s/def ::debug boolean?)
(s/def ::date-header boolean?)
(s/def ::content-type-header boolean?)
(s/def ::server-header boolean?)
(s/def ::idle-timeout-seconds (s/and int? #(<= 0 %)))
(s/def ::routes (s/coll-of ::route :distinct true :min-count 1))

(s/def ::server-config (s/keys :req-un [::port]
                               :opt-un [::instances
                                        ::middleware
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
                :debug                false
                :date-header          false
                :content-type-header  false
                :server-header        false
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
(s/def ::user-agent string?)
(s/def ::enable-user-agent boolean?)
(s/def ::proxy-type #{:http :sock4 :sock5})
(s/def ::proxy (s/keys :req-un [::host ::port ::proxy-type]))
(s/def ::force-sni boolean?)
(s/def ::ssl boolean?)

(s/def ::client-config (s/keys :opt-un [::compression
                                        ::default-host
                                        ::default-port
                                        ::debug
                                        ::force-sni
                                        ::keep-alive
                                        ::keep-alive-timeout-seconds
                                        ::connect-timeout-seconds
                                        ::idle-timeout-seconds
                                        ::max-redirects
                                        ::user-agent
                                        ::enable-user-agent
                                        ::proxy
                                        ::ssl]))

(comment
  (let [config {:keep-alive                 false
                :keep-alive-timeout-seconds 60
                :idle-timeout-seconds       0
                :connect-timeout-seconds    60
                :debug                      false
                :default-port               80
                :default-host               "localhost"
                :max-redirects              16
                :user-agent                 "Donkey-Client"
                :enable-user-agent          true
                :proxy-type                 {:host "localhost"
                                             :port 3128
                                             :type :http|:sock4|:sock5}
                :compression                false
                :middleware                 []}]))


;; ------- Client Request Specification ------- ;;


(s/def ::uri ::not-blank)
(s/def ::bearer-token ::not-blank)
(s/def ::basic-auth (s/map-of #{"id" "password"} ::not-blank))
(s/def ::query-params (s/every string? :kind map?))
(s/def ::headers (s/every string? :kind map?))
(s/def ::body (some-fn bytes? string? #(instance? InputStream %)))

(s/def ::client-request (s/keys :req-un [::method ::handler]
                                :opt-un [::uri
                                         ::host
                                         ::port
                                         ::idle-timeout-seconds
                                         ::bearer-token
                                         ::basic-auth
                                         ::query-params
                                         ::headers
                                         ::body]))
