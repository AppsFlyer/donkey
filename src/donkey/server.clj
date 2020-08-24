(ns donkey.server
  (:require [donkey.metrics :refer [get-metrics-options]]
            [donkey.route :refer [get-router-definition]])
  (:import (io.vertx.core AsyncResult VertxOptions Handler)
           (io.vertx.core.http HttpServerOptions)
           (com.appsflyer.donkey.server ServerConfig Server)
           (com.appsflyer.donkey.server.exception ServerInitializationException ServerShutdownException)
           (com.appsflyer.donkey.route.ring RingRouteCreatorSupplier)
           (io.vertx.core.http HttpServerOptions)))

(defn- ^HttpServerOptions get-server-options
  "docstring"
  [opts]
  (doto (HttpServerOptions.)
    (.setPort (int (:port opts 8080)))
    (.setHost ^String (:host opts "0.0.0.0"))
    (.setLogActivity ^boolean (:debug opts false))
    (.setIdleTimeout (int (:idle-timeout-seconds opts 0)))
    (.setCompressionSupported (:compression opts false))
    (.setDecompressionSupported (:compression opts false))))

(defn- ^VertxOptions get-vertx-options [opts]
  (let [vertx-options (VertxOptions.)]
    (.setPreferNativeTransport vertx-options true)
    (.setEventLoopPoolSize
      vertx-options (int (:event-loops opts (-> (Runtime/getRuntime) .availableProcessors))))
    (when-let [worker-threads (:worker-threads opts)]
      (.setWorkerPoolSize vertx-options (int worker-threads)))
    (when (:metrics-enabled opts)
      (.setMetricsOptions vertx-options (get-metrics-options opts)))
    vertx-options))

(defn ^ServerConfig get-server-config [opts]
  (ServerConfig.
    (get-vertx-options opts)
    (get-server-options opts)
    (RingRouteCreatorSupplier.)
    (get-router-definition opts)))

(deftype EventHandler [impl]
  Handler
  (handle [_this event]
    (impl event)))

(defprotocol HttpServer
  (start [this]
    "Start the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed. Otherwise
    resolved with nil.")
  (start-sync [this]
    "Start the server synchronously.
    Blocks the calling thread until server initialization
    is complete. Throws an ExceptionInfo if the operation failed.")
  (stop [this]
    "Stop the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed. Otherwise
    resolved with nil.")
  (stop-sync [this]
    "Stop the server synchronously.
    Blocks the calling thread until all server resources are terminated.
    Throws an ExceptionInfo if the operation failed.")
  (await-termination [this]
    "Blocks the calling thread until a shutdown signal is received
    by the JVM. Useful in order to prevent exiting the '-main' function.
    Throws an InterruptedException."))

(defrecord DonkeyServer [^Server impl]
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
