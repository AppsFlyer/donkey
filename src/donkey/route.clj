(ns donkey.route
  (:import (io.vertx.ext.web RoutingContext)
           (io.vertx.core Promise)
           (io.vertx.core.http HttpMethod)
           (com.appsflyer.donkey.route RouteDescriptor PathDescriptor PathDescriptor$MatchType HandlerMode)
           (java.util ArrayList)))

(defn- keyword->MatchType [matchType]
  (if (= matchType :regex)
    PathDescriptor$MatchType/REGEX
    PathDescriptor$MatchType/SIMPLE))

(defn- add-path [^RouteDescriptor route route-map]
  (when-let [path (:path route-map)]
    (.path route (PathDescriptor. path (keyword->MatchType (:match-type route-map)))))
  route)

(defn- ^HttpMethod keyword->HttpMethod [method]
  (-> method
      name
      .toUpperCase
      HttpMethod/valueOf))

(defn- keyword->HandlerMode [val]
  (case val
    :blocking HandlerMode/BLOCKING
    :non-blocking HandlerMode/NON_BLOCKING))

(defn- add-methods [^RouteDescriptor route route-map]
  (doseq [method (:method route-map [])]
    (.addMethod route (keyword->HttpMethod method)))
  route)

(defn- add-consumes [^RouteDescriptor route route-map]
  (doseq [content-type (:consume route-map [])]
    (.addConsumes route content-type))
  route)

(defn- add-produces [^RouteDescriptor route route-map]
  (doseq [content-type (:produce route-map [])]
    (.addProduces route content-type))
  route)

(defn- wrap-blocking-handler [handler]
  (fn [^RoutingContext ctx]
    (if-let [request (.get ctx "ring-request")]
      (handler request)
      (throw (IllegalStateException. "Routing context is missing 'ring-request'")))))

(defn- wrap-handler [handler]
  (fn [^RoutingContext ctx]
    (if-let [request (.get ctx "ring-request")]
      (let [promise (Promise/promise)
            respond (fn [res] (.complete promise res))
            raise (fn [ex] (.fail promise ^Throwable ex))]
        (handler request respond raise)
        (.future promise))
      (throw (IllegalStateException. "Routing context is missing 'ring-request'")))))

(defn- add-handler-mode [^RouteDescriptor route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- add-handler [^RouteDescriptor route route-map]
  (if (= (:handler-mode route-map) :blocking)
    (.handler route (wrap-blocking-handler (:handler route-map)))
    (.handler route (wrap-handler (:handler route-map))))
  route)

(defn get-route-descriptors [opts]
  (reduce (fn [res route-map]
            (doto res
              (.add (-> (RouteDescriptor.)
                        (add-path route-map)
                        (add-methods route-map)
                        (add-consumes route-map)
                        (add-produces route-map)
                        (add-handler-mode route-map)
                        (add-handler route-map)))))
          (ArrayList. (count (:routes opts)))
          (:routes opts)))
