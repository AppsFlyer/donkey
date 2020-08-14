(ns donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Handler)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.route PathDescriptor$MatchType PathDescriptor HandlerMode)
           (com.appsflyer.donkey.route.ring RingRouteDescriptor)
           (com.appsflyer.donkey.route.handler.ring Constants)))

(defn- keyword->MatchType [matchType]
  (if (= matchType :regex)
    PathDescriptor$MatchType/REGEX
    PathDescriptor$MatchType/SIMPLE))

(defn- add-path [^RingRouteDescriptor route route-map]
  (when-let [path (:path route-map)]
    (->> (:match-type route-map)
         keyword->MatchType
         (PathDescriptor. path)
         (.path route)))
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

(defn- get-handler-argument [^RoutingContext ctx]
  (if-let [last-response (.get ctx Constants/LAST_HANDLER_RESPONSE_FIELD)]
    last-response
    (throw (IllegalStateException.
             (format "Could not find '%s' in RoutingContext"
                     Constants/LAST_HANDLER_RESPONSE_FIELD)))))

(defn- ^Handler wrap-blocking-handler [handler]
  (reify Handler
    (handle [_this ctx]
      (if-let [handler-argument (get-handler-argument ^RoutingContext ctx)]
        (try
          (-> ^RoutingContext ctx
              (.put Constants/LAST_HANDLER_RESPONSE_FIELD (handler handler-argument))
              .next)
          (catch Throwable ex
            (.fail ^RoutingContext ctx ^Throwable ex)))))))

(defn- ^Handler wrap-handler [handler]
  (reify Handler
    (handle [_this ctx]
      (letfn [(respond [res]
                (-> ^RoutingContext ctx
                    (.put Constants/LAST_HANDLER_RESPONSE_FIELD res)
                    .next))
              (raise [ex] (.fail ^RoutingContext ctx ^Throwable ex))]

        (if-let [handler-argument (get-handler-argument ctx)]
          (try
            (handler handler-argument respond raise)
            (catch Throwable ex
              (.fail ^RoutingContext ctx ^Throwable ex))))))))

(defn- add-handler-mode [^RingRouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- add-async-handlers [^RingRouteDescriptor route handlers]
  (doseq [handler handlers]
    (.addHandler route (wrap-handler handler))))

(defn- add-blocking-handlers [^RingRouteDescriptor route handlers]
  (doseq [handler handlers]
    (.addHandler route (wrap-blocking-handler handler))))

(defn- add-handlers [^RingRouteDescriptor route route-map]
  (if (= (:handler-mode route-map) :blocking)
    (add-blocking-handlers route (:handlers route-map))
    (add-async-handlers route (:handlers route-map)))
  route)

(defn get-route-descriptors [routes]
  (reduce (fn [res route-map]
            (doto ^List res
              (.add (-> (RingRouteDescriptor.)
                        (add-path route-map)
                        (add-methods route-map)
                        (add-consumes route-map)
                        (add-produces route-map)
                        (add-handler-mode route-map)
                        (add-handlers route-map)))))
          (ArrayList. (count routes))
          routes))
