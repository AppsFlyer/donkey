(ns donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Handler)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.route PathDescriptor$MatchType HandlerMode PathDescriptor RouteDescriptor)
           (com.appsflyer.donkey.route.handler Constants)
           (com.appsflyer.donkey.route.handler RouterDefinition Middleware)))

(defn- keyword->MatchType [matchType]
  (if (= matchType :regex)
    PathDescriptor$MatchType/REGEX
    PathDescriptor$MatchType/SIMPLE))

(defn- add-path [^RouteDescriptor route route-map]
  (when-let [path (:path route-map)]
    (->> (:match-type route-map)
         keyword->MatchType
         (PathDescriptor/create path)
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

(defn- add-methods [^RouteDescriptor route route-map]
  (doseq [method (:methods route-map [])]
    (.addMethod route (keyword->HttpMethod method)))
  route)

(defn- add-consumes [^RouteDescriptor route route-map]
  (doseq [^String content-type (:consumes route-map [])]
    (.addConsumes route content-type))
  route)

(defn- add-produces [^RouteDescriptor route route-map]
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

(defmacro chain-middleware [& args]
  (let [funs# (rseq (vec args))]
    `(-> identity ~@funs#)))

(defn- ->Middleware [middleware]
  (let [handler-mode (:handler-mode middleware)
        ^Handler route-handler (if (= handler-mode :blocking)
                                 ->BlockingRouteHandler
                                 ->RouteHandler)]
    (-> (:handlers middleware)
        chain-middleware
        route-handler
        (Middleware. (keyword->HttpMethod handler-mode)))))

(defn- add-handler-mode [^RouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- create-middleware [middleware]
  (when (seq (:handlers middleware))
    (->Middleware middleware)))

(defn- add-middleware [^RouteDescriptor route route-map]
  (when-let [middleware (create-middleware (:middleware route-map))]
    (.middleware route middleware)))

(defn- add-async-handlers [^RouteDescriptor route handlers]
  (doseq [handler handlers]
    (.addHandler route ^Handler (->RouteHandler handler))))

(defmulti
  ^:private add-handler
  (fn [^RouteDescriptor _route route-map]
    (:handler-mode route-map)))

(defmethod
  ^:private add-handler :blocking [^RouteDescriptor route route-map]
  (.addHandler route (->BlockingRouteHandler (:handler route-map)))
  route)

(defmethod
  ^:private add-handler :default [^RouteDescriptor route route-map]
  (.addHandler route (->RouteHandler (:handler route-map)))
  route)

(defn- map->RouteDescriptor [route-map]
  (-> (RouteDescriptor/create)
      (add-path route-map)
      (add-methods route-map)
      (add-consumes route-map)
      (add-produces route-map)
      (add-handler-mode route-map)
      (add-middleware route-map)
      (add-handler route-map)))

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

(defn get-router-definition [opts]
  (RouterDefinition.
    (create-route-descriptors (:routes opts))
    (create-middleware (:middleware opts))))
