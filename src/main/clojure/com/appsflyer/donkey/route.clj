;
; Copyright 2020 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns com.appsflyer.donkey.route
  (:import (io.vertx.core Handler)
           (io.vertx.ext.web RoutingContext)
           (io.vertx.core.http HttpMethod)
           (java.util ArrayList List)
           (com.appsflyer.donkey.server.route PathDefinition$MatchType
                                              RouteList
                                              HandlerMode
                                              PathDefinition
                                              RouteDefinition)
           (com.appsflyer.donkey.server.ring.handler RingHandler)
           (com.appsflyer.donkey.server.exception StatusCodeAware)))

(defn- keyword->MatchType [matchType]
  (if (= matchType :regex)
    PathDefinition$MatchType/REGEX
    PathDefinition$MatchType/SIMPLE))

(defn- add-path [^RouteDefinition route route-map]
  (when-let [path (:path route-map)]
    (->> (:match-type route-map)
         keyword->MatchType
         (PathDefinition/create path)
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

(defn- add-methods [^RouteDefinition route route-map]
  (doseq [method (:methods route-map [])]
    (.addMethod route (keyword->HttpMethod method)))
  route)

(defn- add-consumes [^RouteDefinition route route-map]
  (doseq [^String content-type (:consumes route-map [])]
    (.addConsumes route content-type))
  route)

(defn- add-produces [^RouteDefinition route route-map]
  (doseq [^String content-type (:produces route-map [])]
    (.addProduces route content-type))
  route)

(defn- get-last-handler-result
  "The RoutingContext can maintain arbitrary data. We use it to pass the
  Ring request / response maps between the clojure and java layers.
  When the java layer creates the request map, it adds it to the context
  so it is available in the clojure layer. When the clojure layer creates a
  response map, it adds it to the context so it is available in the java layer.

  * Note about thread safety:
  The values are immutable clojure maps, and therefore thread safe.
  The routing context is always handled by the same thread, and therefore thread
  safe."
  [^RoutingContext ctx]
  (if-let [last-response (.get ctx RingHandler/RING_HANDLER_RESULT)]
    last-response
    (throw (IllegalStateException.
             (format "Could not find '%s' in RoutingContext"
                     RingHandler/RING_HANDLER_RESULT)))))

(defn- handle-response [^RoutingContext ctx res]
  (let [failed (.failed ctx)
        ended (-> ctx .response .ended)]
    (when-not (or failed ended)
      (.next (.put ctx RingHandler/RING_HANDLER_RESULT res)))))

(defn- handle-exception [^RoutingContext ctx ex]
  (if (instance? StatusCodeAware ex)
    (.fail ^RoutingContext ctx (.code ^StatusCodeAware ex) ^Throwable ex)
    (.fail ^RoutingContext ctx ^Throwable ex)))

(deftype RouteHandler [fun]
  RingHandler
  (handle [_this ctx]
    (let [respond (partial handle-response ctx)
          raise (partial handle-exception ctx)]
      (try
        (fun (get-last-handler-result ctx) respond raise)
        (catch Throwable ex
          (raise ex))))))

(deftype BlockingRouteHandler [fun]
  RingHandler
  (handle [_this ctx]
    (try
      (-> ^RoutingContext ctx
          (.put RingHandler/RING_HANDLER_RESULT
                (fun (get-last-handler-result ctx)))
          .next)
      (catch Throwable ex
        (handle-exception ctx ex)))))

(defmulti ^:private create-handler (fn [route-map]
                                     (:handler-mode route-map :non-blocking)))

(defmethod ^:private
  create-handler :blocking [route-map]
  (->BlockingRouteHandler (:handler route-map)))

(defmethod ^:private
  create-handler :non-blocking [route-map]
  (->RouteHandler (:handler route-map)))

(defn- add-handler [^RouteDefinition route route-map]
  (.handler route ^Handler (create-handler route-map)))

(defn- add-handler-mode [^RouteDefinition route route-map]
  (when-let [handler-mode (:handler-mode route-map)]
    (.handlerMode route (keyword->HandlerMode handler-mode)))
  route)

(defn- map->RouteDefinition [route-map]
  (->
    (RouteDefinition/create)
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
        ; initialize the middleware
        (comp-fn handler)))))

(defn- create-route-definitions
  "Returns a List of RouteDefinition"
  [opts]
  (reduce
    (fn [res route]
      (let [handler (compose-middleware route (:middleware opts))]
        (doto ^List res
          (.add
            (map->RouteDefinition
              (assoc route :handler handler))))))
    (ArrayList. (int (count (:routes opts))))
    (:routes opts)))

(defn map->RouteList [opts]
  (RouteList. (create-route-definitions opts)))
