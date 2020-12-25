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
  The server options are used to define basic things such as the host and port
  the server should listen to, as well as lower level connection parameters."
  [{:keys [port
           host
           debug
           tcp-no-delay
           tcp-quick-ack
           tcp-fast-open
           accept-backlog
           socket-linger-seconds
           idle-timeout-seconds
           keep-alive
           compression
           decompression]
    :or   {host                  "0.0.0.0"
           tcp-no-delay          true
           tcp-quick-ack         false
           tcp-fast-open         false
           accept-backlog        1024
           socket-linger-seconds -1
           idle-timeout-seconds  30
           compression           true
           decompression         true}}]

  (doto (HttpServerOptions.)
    (.setPort (int port))
    (.setHost ^String host)
    (.setTcpNoDelay (boolean tcp-no-delay))
    (.setTcpQuickAck (boolean tcp-quick-ack))
    (.setTcpFastOpen (boolean tcp-fast-open))
    (.setAcceptBacklog (int accept-backlog))
    (.setSoLinger (int socket-linger-seconds))
    (.setIdleTimeout (int idle-timeout-seconds))
    (.setLogActivity (boolean debug))
    (.setTcpKeepAlive (boolean keep-alive))
    (.setCompressionSupported (boolean compression))
    (.setDecompressionSupported (boolean decompression))))

(defn ^ServerConfig map->ServerConfig
  "Creates and returns a ServerConfig object from the opts map."
  [{:keys [vertx
           instances
           debug
           date-header
           content-type-header
           server-header]
    :or   {instances (CpuCoreSensor/availableProcessors)}
    :as   opts}]
  (let [builder (doto (ServerConfig/builder)
                  (.serverOptions (map->HttpServerOptions opts))
                  (.routeCreatorFactory (RingRouteCreatorFactory/create))
                  (.routeList (map->RouteList opts))
                  (.vertx vertx)
                  (.instances instances)
                  (.debug (boolean debug))
                  (.addDateHeader (boolean date-header))
                  (.addContentTypeHeader (boolean content-type-header))
                  (.addServerHeader (boolean server-header)))
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
