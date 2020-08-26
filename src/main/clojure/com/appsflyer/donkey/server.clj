(ns com.appsflyer.donkey.server
  (:require [com.appsflyer.donkey.metrics :refer [get-metrics-options]]
            [com.appsflyer.donkey.route :refer [get-router-definition]])
  (:import (io.vertx.core AsyncResult VertxOptions Handler)
           (io.vertx.core.http HttpServerOptions)
           (com.appsflyer.donkey.server Server ServerConfig)
           (com.appsflyer.donkey.server.exception ServerInitializationException ServerShutdownException)
           (com.appsflyer.donkey.route.ring RingRouteCreatorSupplier)
           (com.appsflyer.donkey.util DebugUtil)))

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

(defn- ^VertxOptions get-vertx-options
  "Creates and returns a VertxOptions object from the opts map.
  The vertx options are used to initialise the Vertx object which is an
  integral part of the server. It allows configuring thread pools,
  clustering, and metrics."
  [opts]
  (let [vertx-options (VertxOptions.)]
    (.setPreferNativeTransport vertx-options true)
    (.setEventLoopPoolSize
      vertx-options (int (:event-loops opts (-> (Runtime/getRuntime) .availableProcessors))))
    (when-let [worker-threads (:worker-threads opts)]
      (.setWorkerPoolSize vertx-options (int worker-threads)))
    (when (:metrics-enabled opts)
      (.setMetricsOptions vertx-options (get-metrics-options opts)))
    vertx-options))

(defn ^ServerConfig get-server-config
  "Creates and returns a ServerConfig object from the opts map.
  See the ServerConfig docs for more information."
  [opts]
  (let [builder (doto (ServerConfig/builder)
                  (.vertxOptions (get-vertx-options opts))
                  (.serverOptions (get-server-options opts))
                  (.routeCreatorSupplier (RingRouteCreatorSupplier.))
                  (.routerDefinition (get-router-definition opts))
                  (.debug (:debug opts false))
                  (.addDateHeader (:date-header opts false))
                  (.addContentTypeHeader (:content-type-header opts false))
                  (.addServerHeader (:server-header opts false)))
        config (.build builder)]
    ; We need to initialize debug logging before a Logger
    ; is created, so SLF4J will use Logback instead of another provider.
    (when (.debug config)
      (DebugUtil/enable))
    config))

(deftype EventHandler [impl]
  Handler
  (handle [_this event]
    (impl event)))

(defprotocol HttpServer
  (start [this]
    "Start the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed.
    Otherwise resolved with nil.")
  (start-sync [this]
    "Start the server synchronously.
    Blocks the calling thread until server initialization completes.
    Throws an ExceptionInfo if the operation failed.")
  (stop [this]
    "Stop the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed.
    Otherwise resolved with nil.")
  (stop-sync [this]
    "Stop the server synchronously.
    Blocks the calling thread until all server resources are terminated.
    Throws an ExceptionInfo if the operation failed.")
  (await-termination [this]
    "Blocks the calling thread until a shutdown signal is received
    by the JVM. Useful in order to prevent exiting the '-main' function.
    Throws an InterruptedException."))

(deftype DonkeyServer [^Server impl]
  HttpServer
  (start [_this]
    (let [res (promise)]
      (.onComplete
        (.start ^Server impl)
        (->EventHandler
          (fn [^AsyncResult async-res]
            (if (.failed async-res)
              (deliver res (ex-info "Server initialization failed" {} (.cause async-res)))
              (deliver res nil)))))
      res))

  (start-sync [_this]
    (try
      (.startSync impl)
      (catch ServerInitializationException ex
        (throw (ex-info (.getMessage ex) {} ex)))))

  (stop [_this]
    (let [res (promise)]
      (.onComplete
        (.shutdown ^Server impl)
        (->EventHandler
          (fn [^AsyncResult async-res]
            (if (.failed async-res)
              (deliver res (ex-info "Server shutdown failed" {} (.cause async-res)))
              (deliver res nil)))))
      res))

  (stop-sync [_this]
    (try
      (.shutdownSync impl)
      (catch ServerShutdownException ex
        (throw (ex-info (.getMessage ex) ex)))))

  (await-termination [_this]
    (.awaitTermination ^Server impl)))
