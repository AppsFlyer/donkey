;
; Copyright 2020-2021 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
;

(ns com.appsflyer.donkey.resources
  (:import (java.util ArrayList List)
           (com.appsflyer.donkey.server.route PathDefinition$MatchType
                                              RouteList
                                              PathDefinition
                                              RouteDefinition)
           (com.appsflyer.donkey.server.handler StaticResourcesConfig
                                                StaticResourcesConfig$Builder
                                                StaticResourcesHandler)
           (java.time Duration)))

;; The path is always matched as a regular expression
;; There might be a bug in Vert.x's StaticHandlerImpl as far as exact matches
;; are concerned.
(defn- add-path [^RouteDefinition route route-map]
  (when-let [path (:path route-map)]
    (.path route (PathDefinition/create path PathDefinition$MatchType/REGEX)))
  route)

(defn- add-produces [^RouteDefinition route route-map]
  (doseq [^String content-type (:produces route-map [])]
    (.addProduces route content-type))
  route)

(defn- add-handler [^RouteDefinition route ^StaticResourcesHandler handler]
  (.handler route handler))

(defn- ^List create-route-definitions
  "Returns a List of static RouteDefinition"
  [opts]
  (reduce
    (fn [res route]
      (doto ^List res
        (.add (->
                (RouteDefinition/create)
                (add-path route)
                (add-produces route)
                (add-handler (:handler opts))))))
    (ArrayList. (int (count (:routes opts))))
    (:routes opts)))

(defn- ^StaticResourcesConfig create-static-resources-config
  [{:keys [resources-root
           index-page
           enable-caching
           max-age-seconds
           local-cache-duration-seconds
           local-cache-size]}]
  (let [builder (cond->
                  (doto (StaticResourcesConfig$Builder.))
                  resources-root (.resourcesRoot resources-root)
                  index-page (.indexPage index-page)
                  (boolean? enable-caching) (.enableCaching enable-caching)
                  (int? max-age-seconds) (.maxAge (Duration/ofSeconds max-age-seconds))
                  (int? local-cache-duration-seconds) (.localCacheDuration (Duration/ofSeconds local-cache-duration-seconds))
                  (int? local-cache-size) (.localCacheSize local-cache-size))]
    (.build builder)))

(defn map->RouteList [opts]
  (when (some? opts)
    (let [config (create-static-resources-config opts)]
      (RouteList/from
        (create-route-definitions
          (assoc opts :handler (StaticResourcesHandler/create config)))))))
