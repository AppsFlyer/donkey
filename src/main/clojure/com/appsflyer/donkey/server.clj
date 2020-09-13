(ns com.appsflyer.donkey.server
  (:require [com.appsflyer.donkey.route :refer [get-router-definition]]
            [com.appsflyer.donkey.result])
  (:import (io.vertx.core.http HttpServerOptions)
           (io.vertx.core.impl.cpu CpuCoreSensor)
           (com.appsflyer.donkey.server Server ServerConfig)
           (com.appsflyer.donkey.server.exception ServerInitializationException ServerShutdownException)
           (com.appsflyer.donkey.route.ring RingRouteCreatorSupplier)
           (com.appsflyer.donkey.util DebugUtil)
           (com.appsflyer.donkey.result FutureResult)))

(defn- ^HttpServerOptions get-server-options
  "Creates and returns an HttpServerOptions object from the opts map.
  The server options are use to define basic things such as the host and port
  the server should listen to, as well as lower level connection parameters."
  [opts]
  (doto (HttpServerOptions.)
    (.setPort (int (:port opts 8080)))
    (.setHost ^String (:host opts "0.0.0.0"))
    (.setLogActivity ^boolean (:debug opts false))
    (.setIdleTimeout (int (:idle-timeout-seconds opts 0)))
    (.setCompressionSupported (:compression opts false))
    (.setDecompressionSupported (:compression opts false))))

(defn ^ServerConfig get-server-config
  "Creates and returns a ServerConfig object from the opts map.
  See the ServerConfig docs for more information."
  [opts]
  (let [builder (doto (ServerConfig/builder)
                  (.vertx (:vertx opts))
                  (.serverOptions (get-server-options opts))
                  (.routeCreatorSupplier (RingRouteCreatorSupplier.))
                  (.routerDefinition (get-router-definition opts))
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
    (FutureResult. (.start ^Server impl)))
  (stop [_this]
    (FutureResult. (.shutdown ^Server impl)))
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
