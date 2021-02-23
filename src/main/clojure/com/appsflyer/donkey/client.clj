;
; Copyright 2020-2021 AppsFlyer
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

(ns com.appsflyer.donkey.client
  (:require [com.appsflyer.donkey.request])
  (:import (com.appsflyer.donkey.client ClientConfig Client)
           (com.appsflyer.donkey.request AsyncRequest)
           (io.vertx.core.http HttpClientOptions)
           (io.vertx.core.net ProxyOptions ProxyType)
           (io.vertx.ext.web.client WebClientOptions)
           (clojure.lang IPersistentMap)))

(defn- ^ProxyType keyword->ProxyType [type]
  (ProxyType/valueOf (-> type name .toUpperCase)))

(defn map->ProxyOptions [opts]
  (doto (ProxyOptions.)
    (.setHost (:host opts))
    (.setPort (:port opts))
    (.setType (keyword->ProxyType (:proxy-type opts)))))

(defn- ^HttpClientOptions map->HttpClientOptions
  "Creates and returns an HttpClientOptions object from the opts map.
  The client options are used to define global default settings that will be
  applied to each request. Some of these settings can be overridden on a pair
  request basis."
  [{:keys [force-sni
           enable-user-agent
           user-agent
           default-host
           default-port
           follow-redirects
           max-redirects
           keep-alive
           keep-alive-timeout-seconds
           connect-timeout-seconds
           idle-timeout-seconds
           debug
           proxy-options
           compression
           ssl]
    :or   {force-sni         true
           enable-user-agent false
           keep-alive        false
           debug             false
           follow-redirects  true
           user-agent        "Donkey-Client"}}]

  (cond->
    (doto (WebClientOptions.)
      (.setForceSni ^boolean force-sni)
      (.setLogActivity ^boolean debug)
      (.setUserAgentEnabled ^boolean enable-user-agent)
      (.setUserAgent user-agent)
      (.setKeepAlive ^boolean keep-alive)
      (.setFollowRedirects ^boolean follow-redirects))
    keep-alive-timeout-seconds (.setKeepAliveTimeout (int keep-alive-timeout-seconds))
    proxy-options (.setProxyOptions ^ProxyOptions (map->ProxyOptions proxy-options))
    default-host (.setDefaultHost ^String default-host)
    default-port (.setDefaultPort (int default-port))
    max-redirects (.setMaxRedirects (int max-redirects))
    connect-timeout-seconds (.setConnectTimeout (int (* 1000 connect-timeout-seconds)))
    idle-timeout-seconds (.setIdleTimeout (int idle-timeout-seconds))
    compression (.setTryUseCompression true)
    ssl (.setSsl true)
    (and ssl (not default-port)) (.setDefaultPort (int 443))))

(defn ^ClientConfig map->ClientConfig
  "Creates and returns a ClientConfig object from the opts map.
  See the ClientConfig docs for more information."
  [opts]
  (-> (ClientConfig/builder)
      (.vertx (:vertx opts))
      (.clientOptions (map->HttpClientOptions opts))
      .build))

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

    :url [string] The absolute location of the resource being requested.
      For example, `http://www.example.com:8080/api/v1/users`. When an `:url` is
      supplied then the `:uri`, `:port`, `:host` and `:ssl` keys are ignored.

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

    :basic-auth-options [map] Configure the request to perform basic access
      authentication. In basic HTTP authentication, a request contains a header
      field of the form 'Authorization: Basic <credentials>',
      where credentials is the base64 encoding of id and password joined by a
      colon. The map must contain these fields:
      - id [string] The id used for the authentication.
      - password [string] The password used for the authentication.")
  (stop [this]
    "Stops the client and releases any resources associated with it."))

(deftype DonkeyClient [^Client impl]
  HttpClient
  (request [_this opts]
    (AsyncRequest. impl (.request ^Client impl ^IPersistentMap opts)))
  (stop [_this]
    (.shutdown impl)))
