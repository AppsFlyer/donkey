(ns donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Handler)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.route PathDescriptor$MatchType PathDescriptor HandlerMode)
           (com.appsflyer.donkey.route.ring RingRouteDescriptor)
           (com.appsflyer.donkey.route.handler.ring Constants RingHandlerFactory)
           (com.appsflyer.donkey.route.handler HandlerConfig Middleware)))

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

(deftype RouteHandler [impl]
  Handler
  (handle [_this ctx]
    (letfn [(respond [res]
              (-> ^RoutingContext ctx
                  (.put Constants/LAST_HANDLER_RESPONSE_FIELD res)
                  .next))
            (raise [ex] (.fail ^RoutingContext ctx ^Throwable ex))]
      (try
        (impl (get-handler-argument ctx) respond raise)
        (catch Throwable ex
          (.fail ^RoutingContext ctx ^Throwable ex))))))

(deftype BlockingRouteHandler [impl]
  Handler
  (handle [_this ctx]
    (try
      (-> ^RoutingContext ctx
          (.put Constants/LAST_HANDLER_RESPONSE_FIELD
                (impl (get-handler-argument ^RoutingContext ctx)))
          .next)
      (catch Throwable ex
        (.fail ^RoutingContext ctx ^Throwable ex)))))

(defn- add-handler-mode [^RingRouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- add-async-handlers [^RingRouteDescriptor route handlers]
  (doseq [handler handlers]
    (.addHandler route ^Handler (->RouteHandler handler))))

(defn- add-blocking-handlers [^RingRouteDescriptor route handlers]
  (doseq [handler handlers]
    (.addHandler route ^Handler (->BlockingRouteHandler handler))))

(defn- add-handlers [^RingRouteDescriptor route route-map]
  (if (= (:handler-mode route-map) :blocking)
    (add-blocking-handlers route (:handlers route-map))
    (add-async-handlers route (:handlers route-map)))
  route)

(defn- map->RouteDescriptor [route-map]
  (-> (RingRouteDescriptor.)
      (add-path route-map)
      (add-methods route-map)
      (add-consumes route-map)
      (add-produces route-map)
      (add-handler-mode route-map)
      (add-handlers route-map)))

(defn- map->Middleware [middleware-map]
  (if (= :blocking (:handler-mode middleware-map))
    (Middleware. ^Handler (->BlockingRouteHandler (:handler middleware-map)) HandlerMode/BLOCKING)
    (Middleware. ^Handler (->RouteHandler (:handler middleware-map)) HandlerMode/NON_BLOCKING)))

(defn- into-array-list
  "Perform 'fun' on each element in 'col' and return a Java List with the result"
  [col fun]
  (reduce
    (fn [res entry]
      (doto ^List res
        (.add (fun entry))))
    (ArrayList. (count col))
    col))

(defn- create-route-descriptors [routes]
  (into-array-list routes map->RouteDescriptor))

(defn- create-middleware [middleware]
  (into-array-list middleware map->Middleware))

(defn get-handler-config [opts]
  (HandlerConfig.
    (create-route-descriptors (:routes opts))
    (RingHandlerFactory.)
    (create-middleware (:middleware opts))))
