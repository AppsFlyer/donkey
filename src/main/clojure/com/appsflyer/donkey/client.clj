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
      (when-let [timeout (:keep-alive-timeout-seconds opts)]
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
    "Creates an asynchronous HTTP request. Returns an instance of AsyncRequest.
    `opts` map description (all values are optional unless stated otherwise):

    :method [keyword] Required. The HTTP method to use, e.g GET, POST etc'. Must
      be one of :get :post :put :delete :options :head :trace :connect or
      :patch.

    :host [string] The hostname to use for this request. The hostname is the
      part of the URL that comes after the scheme part - e.g `http://`,
      and before the optional port and uri. For example given the URL
      `http://www.example.com:8080/api/v1/users` the host would be
      `www.example.com`. Setting the host when creating a request overrides the
      `:default-host` that was previously set when the client was created.
      Defaults to `localhost`.

    :port [int] The port to use for this request. The port is the part of the
      url that comes after the host, and is preceded by a colon `:`. For
      example, given the URL `http://www.example.com:8080/api/v1/users` the port
      would be `8080`. Setting the port when creating a request overrides the
      `:default-port` that was previously set when the client was created.
      Defaults to `80`.

    :uri [string] The location of the resource being requested. It refers to
      the part after the hostname and optional port of the URL. For example,
      given the URL `http://www.example.com:8080/api/v1/users` the uri would be
      `/api/v1/users`. Defaults to `/`.

    :query-params [map] Mapping of string key value pairs. The pairs will be
      added to the query part of the url. It is also possible to include query
      parameters in the `:uri`, for example - `/api/v1/users?id=1`. In that case
      the key value pairs will be added to the query string.
      Note, if you need to include a key multiple times then you must include it
      in the uri.

    :headers [map] Mapping of string key value pairs. To include multiple values
      for the same key, separate the values with a comma ','. For example,
      `{\"Cache-Control\" \"public, max-age=604800, immutable\"}`

    :idle-timeout-seconds [int] The duration of seconds after which the
      connection will be closed if no data was received. Defaults to never.

    :bearer-token [string] Configure the request to perform bearer token
      authentication. In OAuth 2.0, a request contains a header field of the form
      'Authorization: Bearer <bearerToken>', where bearerToken is the bearer
      token issued by an authorization server to access protected resources.

    :basic-auth [map] Configure the request to perform basic access
      authentication. In basic HTTP authentication, a request contains a header
      field of the form 'Authorization: Basic <credentials>',
      where credentials is the base64 encoding of id and password joined by a
      colon. The map must contain these fields:
        id [string] The id used for the authentication.
        password [string] The password used for the authentication.")
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
