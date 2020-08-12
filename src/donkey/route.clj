(ns donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Promise)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (java.util.function Function)
           (com.appsflyer.donkey.route PathDescriptor PathDescriptor$MatchType HandlerMode)
           (com.appsflyer.donkey.route.ring RingRouteDescriptor)
           (com.appsflyer.donkey.route.handler.ring Constants)))

(defn- keyword->MatchType [matchType]
  (if (= matchType :regex)
    PathDescriptor$MatchType/REGEX
    PathDescriptor$MatchType/SIMPLE))

(defn- add-path [^RingRouteDescriptor route route-map]
  (when-let [path (:path route-map)]
    (.path route (PathDescriptor. path (keyword->MatchType (:match-type route-map)))))
  route)

(defn- ^HttpMethod keyword->HttpMethod [method]
  (-> method
      name
      .toUpperCase
      HttpMethod/valueOf))

(defn- ^HandlerMode keyword->HandlerMode [val]
  (case val
    :blocking HandlerMode/BLOCKING
    :non-blocking HandlerMode/NON_BLOCKING))

(defn- add-methods [^RingRouteDescriptor route route-map]
  (doseq [method (:methods route-map [])]
    (.addMethod route (keyword->HttpMethod method)))
  route)

(defn- add-consumes [^RingRouteDescriptor route route-map]
  (doseq [^String content-type (:consumes route-map [])]
    (.addConsumes route content-type))
  route)

(defn- add-produces [^RingRouteDescriptor route route-map]
  (doseq [^String content-type (:produces route-map [])]
    (.addProduces route content-type))
  route)

(defn- ^Function wrap-blocking-handler [handler]
  (reify Function
    (apply [_this ctx]
      (if-let [request (.get ^RoutingContext ctx Constants/RING_REQUEST_FIELD)]
        (handler request)
        (throw (IllegalStateException.
                 (format "Routing context is missing '%s'" Constants/RING_REQUEST_FIELD)))))))

(defn- ^Function wrap-handler [handler]
  (reify Function
    (apply [_this ctx]
      (if-let [request (.get ^RoutingContext ctx Constants/RING_REQUEST_FIELD)]
        (let [promise (Promise/promise)
              respond (fn [res] (.complete promise res))
              raise (fn [ex] (.fail promise ^Throwable ex))]
          (handler request respond raise)
          (.future promise))
        (throw (IllegalStateException.
                 (format "Routing context is missing '%s'" Constants/RING_REQUEST_FIELD)))))))

(defn- add-handler-mode [^RingRouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- add-handler [^RingRouteDescriptor route route-map]
  (if (= (:handler-mode route-map) :blocking)
    (.handler route (wrap-blocking-handler (:handler route-map)))
    (.handler route (wrap-handler (:handler route-map))))
  route)

(defn get-route-descriptors [opts]
  (reduce (fn [res route-map]
            (doto ^List res
              (.add (-> (RingRouteDescriptor.)
                        (add-path route-map)
                        (add-methods route-map)
                        (add-consumes route-map)
                        (add-produces route-map)
                        (add-handler-mode route-map)
                        (add-handler route-map)))))
          (ArrayList. (count (:routes opts)))
          (:routes opts)))
