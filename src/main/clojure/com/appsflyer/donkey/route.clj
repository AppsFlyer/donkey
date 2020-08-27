(ns com.appsflyer.donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Handler)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.route PathDescriptor$MatchType HandlerMode PathDescriptor RouteDescriptor)
           (com.appsflyer.donkey.route.handler Constants)
           (com.appsflyer.donkey.route.handler RouterDefinition)
           (com.appsflyer.donkey.route.handler.ring RingHandler)))

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

(defn- response-handler [^RoutingContext ctx res]
  (let [failed (.failed ctx)
        ended (-> ctx .response .ended)]
    (when-not (or failed ended)
      (.next (.put ctx Constants/LAST_HANDLER_RESPONSE_FIELD res)))))

(deftype RouteHandler [impl]
  RingHandler
  (handle [_this ctx]
    (let [respond (partial response-handler ctx)
          raise (fn [ex] (.fail ^RoutingContext ctx ^Throwable ex))]
      (try
        (impl (get-handler-argument ctx) respond raise)
        (catch Throwable ex
          (.fail ^RoutingContext ctx ^Throwable ex))))))

(deftype BlockingRouteHandler [impl]
  RingHandler
  (handle [_this ctx]
    (try
      (-> ^RoutingContext ctx
          (.put Constants/LAST_HANDLER_RESPONSE_FIELD
                (impl (get-handler-argument ^RoutingContext ctx)))
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
  (.addHandler route ^Handler (create-handler route-map)))

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

(defn- add-middleware [route-map global-middleware]
  (let [handlers (concat
                   (if (empty? global-middleware) [] global-middleware)
                   (:middleware route-map []))]
    (update route-map :handler (fn [handler]
                                 (if (empty? handlers)
                                   handler
                                   (let [comp-fn (apply comp handlers)]
                                     (comp-fn handler)))))))

(defn- create-route-descriptors [opts]
  (reduce
    (fn [res route]
      (doto ^List res
        (.add (map->RouteDescriptor (add-middleware route (:middleware opts))))))
    (ArrayList. (int (count (:routes opts))))
    (:routes opts)))

(defn get-router-definition [opts]
  (RouterDefinition. (create-route-descriptors opts)))
