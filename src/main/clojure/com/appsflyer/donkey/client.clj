(ns com.appsflyer.donkey.client
  (:import (io.vertx.core AsyncResult Handler)
           (com.appsflyer.donkey.client ClientConfig)
           (com.appsflyer.donkey.util DebugUtil)
           (io.vertx.core.http HttpClientOptions)
           (io.vertx.core.net ProxyOptions ProxyType)
           (io.vertx.ext.web.client WebClientOptions)
           (com.appsflyer.donkey.client.ring RingClient)
           (clojure.lang IPersistentMap)))


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
  when they are not present per request, and different connection settings."
  [opts]
  (let [client-options (WebClientOptions.)]
    (when-let [keep-alive (:keep-alive opts)]
      (.setKeepAlive client-options ^boolean keep-alive)
      (when-let [timeout (:keep-alive-timeout-seconds)]
        (.setKeepAliveTimeout client-options (int timeout))))
    (when-let [proxy (:proxy opts)]
      (.setProxyOptions client-options ^ProxyOptions (get-proxy-options proxy)))
    (when-let [default-host (:default-host opts)]
      (.setDefaultHost client-options ^String default-host))
    (when-let [default-port (:default-port opts)]
      (.setDefaultPort client-options (int default-port)))
    (when-let [max-redirects (:max-redirects opts)]
      (.setMaxRedirects client-options (int max-redirects)))
    (when-let [connect-timeout (:connect-timeout-seconds opts)]
      (.setConnectTimeout client-options (int (* 1000 connect-timeout))))
    (when-let [idle-timeout (:idle-timeout-seconds opts)]
      (.setIdleTimeout client-options (int idle-timeout)))
    (when-let [enable-user-agent (:enable-user-agent opts false)]
      (.setUserAgentEnabled client-options enable-user-agent)
      (if-let [user-agent (:user-agent opts)]
        (.setUserAgent client-options user-agent)
        (.setUserAgent client-options "Donkey-Client")))
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

(deftype ClientResponseHandler [impl]
  Handler
  (handle [_this event]
    (if (.succeeded ^AsyncResult event)
      (impl (.result ^AsyncResult event) nil)
      (impl nil (ex-info (-> ^AsyncResult event .cause .getMessage) {} (.cause ^AsyncResult event))))))

(defprotocol HttpClient
  (request [this opts]
    "Make an asynchronous HTTP request.")
  (stop [this]
    "Stops the client and releases any resources associated with it."))

(deftype DonkeyClient [^RingClient impl]
  HttpClient
  (request [_this opts]
    (when-not (:handler opts)
      (throw (ex-info ":handler missing from request options" opts)))
    (.request
      ^RingClient impl
      ^IPersistentMap (update opts :handler ->ClientResponseHandler)))
  (stop [_this]
    (.shutdown impl)))

