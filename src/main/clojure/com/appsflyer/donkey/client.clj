(ns com.appsflyer.donkey.client
  (:require [com.appsflyer.donkey.request])
  (:import (com.appsflyer.donkey.client ClientConfig)
           (com.appsflyer.donkey.client.ring RingClient)
           (com.appsflyer.donkey.request AsyncRequest)
           (com.appsflyer.donkey.util DebugUtil)
           (io.vertx.core.http HttpClientOptions)
           (io.vertx.core.net ProxyOptions ProxyType)
           (io.vertx.ext.web.client WebClientOptions)
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
  The client options are used to define global default settings that will be applied
  to each request. Some of these settings can be overridden on each request."
  [opts]
  (let [client-options (WebClientOptions.)]
    (.setForceSni client-options (boolean (:force-sni opts true)))
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
    (.setUserAgentEnabled client-options (:enable-user-agent opts false))
    (if-let [user-agent (:user-agent opts)]
      (.setUserAgent client-options user-agent)
      (.setUserAgent client-options "Donkey-Client"))
    (when (:debug opts)
      (.setLogActivity client-options true))
    (when (:compression opts)
      (.setTryUseCompression client-options true))
    (when (:ssl opts)
      (.setSsl client-options true))
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
    (if (.debug config)
      (DebugUtil/enable)
      (DebugUtil/disable))
    config))

(defprotocol HttpClient
  (request [this opts]
    "Creates an asynchronous HTTP request.
    Returns an instance of AsyncRequest")
  (stop [this]
    "Stops the client and releases any resources associated with it."))

(deftype DonkeyClient [^RingClient impl]
  HttpClient
  (request [_this opts]
    (AsyncRequest. impl (.request ^RingClient impl ^IPersistentMap opts)))
  (stop [_this]
    (.shutdown impl)))

(comment
  (->
    (request {:method :get :uri "/foo"})                    ; => Create a request. Request not sent yet. Returns Request object
    (submit #_optional-body)                                ; => Send the request. Returns a FutureResult.
    (on-complete (fn [res ex] (println "success or fail"))) ; => Triggers when the request completes. Returns a FutureResult.
    (on-success (fn [res] (println "success")))             ; => Triggers when the request is successful. Returns a FutureResult.
    (on-fail (fn [ex] (println "fail"))))                   ; => Triggers when the request fails. Returns a FutureResult.

  )
