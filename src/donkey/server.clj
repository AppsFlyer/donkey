(ns donkey.server
  (:require [donkey.metrics :refer [get-metrics-options]]
            [donkey.route :refer [get-route-descriptors]])
  (:import (io.vertx.core.http HttpServerOptions)
           (io.vertx.core.net PemKeyCertOptions JksOptions)
           (com.appsflyer.donkey.server Server ServerConfig)
           (com.appsflyer.donkey.exception ServerInitializationException ServerShutdownException)
           (io.vertx.core AsyncResult VertxOptions Handler)
           (com.appsflyer.donkey.route.handler.ring RingHandlerFactory)))

(defn- set-ssl-options [^HttpServerOptions server-options opts]
  (case (:ssl-type opts)
    :jks (.setKeyStoreOptions server-options (doto (JksOptions.)
                                               (.setPath (:key-store-path opts))
                                               (.setPassword (:key-store-password opts))))
    :pem (.setPemKeyCertOptions server-options (doto (PemKeyCertOptions.)
                                                 (.setKeyPath (:pem-key-path opts))
                                                 (.setCertPath (:pem-cert-path opts)))))
  (.setSsl server-options true))

(defn- ^HttpServerOptions get-server-options
  "docstring"
  [opts]
  (let [server-options (doto (HttpServerOptions.)
                         (.setPort (:port opts 8080))
                         (.setHost (:host opts "0.0.0.0"))
                         (.setLogActivity (:debug opts false))
                         (.setIdleTimeout (:idle-timeout-seconds opts 0))
                         (.setCompressionSupported (:compression opts true))
                         (.setDecompressionSupported (:compression opts true)))]
    (when (:ssl opts)
      (set-ssl-options server-options opts))
    server-options))

(defn- ^VertxOptions get-vertx-options [opts]
  (let [vertx-options (VertxOptions.)]
    (.setPreferNativeTransport vertx-options true)
    (when-let [event-loops (:event-loops opts)]
      (.setEventLoopPoolSize vertx-options (int event-loops)))
    (when-let [worker-threads (:worker-threads opts)]
      (.setWorkerPoolSize vertx-options (int worker-threads)))
    (when (:metrics-enabled opts)
      (.setMetricsOptions vertx-options (get-metrics-options opts)))
    vertx-options))

(defn ^ServerConfig get-server-config [opts]
  (ServerConfig.
    (get-vertx-options opts)
    (get-server-options opts)
    (get-route-descriptors opts)
    (RingHandlerFactory.)))

(defn- make-handler [fun]
  (reify Handler
    (handle [_this event]
      (fun event))))

(defprotocol HttpServer
  (start [this]
    "Start the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed. Otherwise
    resolved with nil.")
  (startSync [this]
    "Start the server synchronously.
    Blocks the calling thread until server initialization
    is complete. Throws an ExceptionInfo if the operation failed.")
  (stop [this]
    "Stop the server asynchronously.
    Returns a promise that will be resolved with an
    ExceptionInfo if the operation failed. Otherwise
    resolved with nil.")
  (stopSync [this]
    "Stop the server synchronously.
    Blocks the calling thread until all server resources are terminated.
    Throws an ExceptionInfo if the operation failed.")
  (awaitTermination [this]
    "Blocks the calling thread until a shutdown signal is received
    by the JVM. Useful in order to prevent exiting the '-main' function.
    Throws an InterruptedException."))

(defrecord DonkeyServer [^Server impl]
  HttpServer
  (start [_this]
    (let [res (promise)]
      (.onComplete
        (.start ^Server impl)
        (make-handler
          (fn [^AsyncResult async-res]
            (if (.failed async-res)
              (deliver res (ex-info "Server initialization failed" {} (.cause async-res)))
              (deliver res nil)))))
      res))

  (startSync [_this]
    (try
      (.startSync impl)
      (catch ServerInitializationException ex
        (throw (ex-info (.getMessage ex) {} ex)))))

  (stop [_this]
    (let [res (promise)]
      (.onComplete
        (.shutdown impl)
        (make-handler
          (fn [^AsyncResult async-res]
            (if (.failed async-res)
              (deliver res (ex-info "Server shutdown failed" {} (.cause async-res)))
              (deliver res nil)))))
      res))

  (stopSync [_this]
    (try
      (.shutdownSync impl)
      (catch ServerShutdownException ex
        (throw (ex-info (.getMessage ex) ex)))))

  (awaitTermination [_this]
    (.awaitTermination impl)))
