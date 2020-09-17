(ns com.appsflyer.donkey.route
  (:import (io.vertx.core Handler)
           (io.vertx.ext.web RoutingContext)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.server.router RouterDefinition)
           (com.appsflyer.donkey.server.route PathDescriptor$MatchType
                                              HandlerMode
                                              PathDescriptor
                                              RouteDescriptor)
           (com.appsflyer.donkey.server.ring.handler RingHandler)))

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

(defn- ^HandlerMode keyword->HandlerMode [val]
  (case val
    :blocking HandlerMode/BLOCKING
    :non-blocking HandlerMode/NON_BLOCKING))

(defn- ^HttpMethod keyword->HttpMethod [method]
  (-> method
      name
      .toUpperCase
      HttpMethod/valueOf))

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

(defn- get-last-handler-response [^RoutingContext ctx]
  (if-let [last-response (.get ctx RingHandler/RING_HANDLER_RESULT)]
    last-response
    (throw (IllegalStateException.
             (format "Could not find '%s' in RoutingContext"
                     RingHandler/RING_HANDLER_RESULT)))))

(defn- response-handler [^RoutingContext ctx res]
  (let [failed (.failed ctx)
        ended (-> ctx .response .ended)]
    (when-not (or failed ended)
      (.next (.put ctx RingHandler/RING_HANDLER_RESULT res)))))

(deftype RouteHandler [fun]
  RingHandler
  (handle [_this ctx]
    (let [respond (partial response-handler ctx)
          raise (fn [ex] (.fail ^RoutingContext ctx ^Throwable ex))]
      (try
        (fun (get-last-handler-response ctx) respond raise)
        (catch Throwable ex
          (.fail ^RoutingContext ctx ^Throwable ex))))))

(deftype BlockingRouteHandler [fun]
  RingHandler
  (handle [_this ctx]
    (try
      (-> ^RoutingContext ctx
          (.put RingHandler/RING_HANDLER_RESULT
                (fun (get-last-handler-response ^RoutingContext ctx)))
          .next)
      (catch Throwable ex
        (.fail ^RoutingContext ctx ^Throwable ex)))))

(defmulti
  ^:private create-handler
  (fn [route-map]
    (:handler-mode route-map :non-blocking)))

(defmethod
  ^:private create-handler :blocking [route-map]
  (->BlockingRouteHandler (:handler route-map)))

(defmethod
  ^:private create-handler :non-blocking [route-map]
  (->RouteHandler (:handler route-map)))

(defn- add-handler [^RouteDescriptor route route-map]
  (.handler route ^Handler (create-handler route-map)))

(defn- add-handler-mode [^RouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- map->RouteDescriptor [route-map]
  (-> (RouteDescriptor/create)
      (add-path route-map)
      (add-methods route-map)
      (add-consumes route-map)
      (add-produces route-map)
      (add-handler-mode route-map)
      (add-handler route-map)))

(defn- compose-middleware
  "The function takes the global middleware that's applied to all routes,
   the route specific middleware, and the route handler, and returns a new
   function that is the composition of all the functions.
   In the simplest case where there's no middleware, it returns the route handler.
   If there's global or route middleware, then it applies the global middleware first,
   then the route specific middleware, and finally the route handler."
  [{:keys [handler middleware] :or {middleware []}} global-middleware]
  (let [handlers (concat
                   (if (empty? global-middleware) [] global-middleware)
                   middleware)]
    (if (empty? handlers)
      handler
      (let [comp-fn (apply comp handlers)]
        (comp-fn handler)))))

(defn- create-route-descriptors
  "Returns a List<RouteDescriptor>"
  [opts]
  (reduce
    (fn [res route]
      (let [handler (compose-middleware route (:middleware opts))]
        (doto ^List res
          (.add
            (map->RouteDescriptor
              (assoc route :handler handler))))))
    (ArrayList. (int (count (:routes opts))))
    (:routes opts)))

(defn get-router-definition [opts]
  (RouterDefinition. (create-route-descriptors opts)))
