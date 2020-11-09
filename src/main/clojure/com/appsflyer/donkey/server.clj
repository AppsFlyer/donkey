;
; Copyright 2020 AppsFlyer
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

(ns com.appsflyer.donkey.server
  (:require [com.appsflyer.donkey.route :refer [map->RouteList]]
            [com.appsflyer.donkey.result])
  (:import (io.vertx.core.http HttpServerOptions)
           (io.vertx.core.impl.cpu CpuCoreSensor)
           (com.appsflyer.donkey.server Server ServerConfig)
           (com.appsflyer.donkey.server.exception ServerInitializationException
                                                  ServerShutdownException)
           (com.appsflyer.donkey.server.ring.route RingRouteCreatorFactory)
           (com.appsflyer.donkey.util DebugUtil)
           (com.appsflyer.donkey FutureResult)))

(defn- ^HttpServerOptions map->HttpServerOptions
  "Creates and returns an HttpServerOptions object from the opts map.
  The server options are use to define basic things such as the host and port
  the server should listen to, as well as lower level connection parameters."
  [opts]
  (doto (HttpServerOptions.)
    (.setPort (int (:port opts)))
    (.setHost ^String (:host opts "0.0.0.0"))
    (.setLogActivity ^boolean (:debug opts false))
    (.setIdleTimeout (int (:idle-timeout-seconds opts 0)))
    (.setTcpKeepAlive ^boolean (:keep-alive opts false))
    (.setCompressionSupported ^boolean (:compression opts false))
    (.setDecompressionSupported ^boolean (:compression opts false))))

(defn ^ServerConfig map->ServerConfig
  "Creates and returns a ServerConfig object from the opts map.
  See the ServerConfig docs for more information."
  [opts]
  (let [builder (doto (ServerConfig/builder)
                  (.vertx (:vertx opts))
                  (.serverOptions (map->HttpServerOptions opts))
                  (.routeCreatorFactory (RingRouteCreatorFactory.))
                  (.routeList (map->RouteList opts))
                  (.instances (:instances opts (CpuCoreSensor/availableProcessors)))
                  (.debug (:debug opts false))
                  (.addDateHeader (:date-header opts false))
                  (.addContentTypeHeader (:content-type-header opts false))
                  (.addServerHeader (:server-header opts false)))
        config (.build builder)]
    ; We need to initialize debug logging before a Logger
    ; is created, so SLF4J will use Logback instead of another provider.
    (if (.debug config)
      (DebugUtil/enable)
      (DebugUtil/disable))
    config))

(defprotocol HttpServer
  (start [this]
    "Start the server asynchronously.
    Returns a FutureResult that will be resolved with an ExceptionInfo if the
    operation failed. Otherwise resolved with nil.")
  (start-sync [this]
    "Start the server synchronously.
    Blocks the calling thread until server initialization completes.
    Throws an ExceptionInfo if the operation failed.")
  (stop [this]
    "Stop the server asynchronously.
    Returns a FutureResult that will be resolved with an ExceptionInfo if the
    operation failed. Otherwise resolved with nil.")
  (stop-sync [this]
    "Stop the server synchronously.
    Blocks the calling thread until all server resources are terminated.
    Throws an ExceptionInfo if the operation failed."))

(deftype DonkeyServer [^Server impl]
  HttpServer
  (start [_this]
    (FutureResult/create (.start ^Server impl)))
  (stop [_this]
    (FutureResult/create (.shutdown ^Server impl)))
  (start-sync [_this]
    (try
      (.startSync impl)
      (catch ServerInitializationException ex
        (throw (ex-info (.getMessage ex) {} ex)))))
  (stop-sync [_this]
    (try
      (.shutdownSync impl)
      (catch ServerShutdownException ex
        (throw (ex-info (.getMessage ex) ex))))))
