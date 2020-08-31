(ns com.appsflyer.donkey.client
  (:import (io.vertx.core AsyncResult)
           (com.appsflyer.donkey.client Client ClientConfig)
           (com.appsflyer.donkey.util DebugUtil)
           (io.vertx.core.http HttpClientOptions)
           (io.vertx.core.net ProxyOptions ProxyType)))


(defn- ^ProxyType keyword->ProxyType [type]
  (ProxyType/valueOf (-> type name .toUpperCase)))

(defn get-proxy-options [proxy]
  (doto (ProxyOptions.)
    (.setHost (:host proxy))
    (.setPort (:port proxy))
    (.setType (keyword->ProxyType (:proxy-type proxy)))))

(defn- ^HttpClientOptions get-client-options
  "Creates and returns an HttpClientOptions object from the opts map.
  The client options are used to define things such as default host / port
  when they are not present per request, and diferent connection settings."
  [opts]
  (let [client-options (HttpClientOptions.)]

    (when-let [keep-alive (:keep-alive opts)]
      (.setKeepAlive client-options ^boolean keep-alive)
      (when-let [timeout (:keep-alive-timeout-seconds)]
        (.setKeepAliveTimeout client-options timeout)))
    (when-let [proxy (:proxy opts)]
      (.setProxyOptions client-options (get-proxy-options proxy)))
    (when-let [default-host (:default-host opts)]
      (.setDefaultHost client-options default-host))
    (when-let [default-port (:default-port opts)]
      (.setDefaultPort client-options default-port))
    (when-let [max-redirects (:max-redirects opts)]
      (.setMaxRedirects client-options max-redirects))
    (when-let [connect-timeout (:connect-timeout-seconds opts)]
      (.setConnectTimeout client-options (* 1000 connect-timeout)))
    (when-let [idle-timeout (:idle-timeout-seconds opts)]
      (.setIdleTimeout client-options idle-timeout))
    (when (:debug opts)
      (.setLogActivity client-options true))
    (when (:compression opts)
      (.setTryUseCompression client-options true))

    client-options))

(defn ^ClientConfig get-client-config
  "Creates and returns a ClientConfig object from the opts map.
  See the ClientConfig docs for more information."
  [opts]
  (let [builder (doto (ClientConfig/builder)
                  (.vertx (:vertx opts))
                  (.clientOptions (get-client-options opts))
                  (.debug (:debug opts false)))
        config (.build builder)]
    ; We need to initialize debug logging before a Logger
    ; is created, so SLF4J will use Logback instead of another provider.
    (when (.debug config)
      (DebugUtil/enable))
    config))

(defprotocol HttpClient
  (request [this opts]
    "Make an asynchronous HTTP request.
    Returns a promise that will be resolved with an AsyncClientResult.")
  (stop [this]
    "Stops the client and releases any resources associated with it."))

(deftype DonkeyClient [^Client impl]
  HttpClient
  (request [_this opts]
    )
  (stop [_this]
    (.shutdown impl)))

(deftype AsyncClientResult [^AsyncResult impl]
  AsyncResult
  (result [_this]
    )
  (cause [_this]
    (ex-info (-> impl .cause .getMessage) {} (.cause impl)))
  (succeeded [_this]
    (.succeeded impl))
  (failed [_this]
    (.failed impl)))
