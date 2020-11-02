;
; Copyright 2020 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns com.appsflyer.donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.client :as client]
            [com.appsflyer.donkey.metrics :as metrics]
            [com.appsflyer.donkey.donkey-spec :as donkey-spec])
  (:import (com.appsflyer.donkey.server Server)
           (com.appsflyer.donkey.client.ring RingClient)
           (io.vertx.core Vertx VertxOptions)
           (io.vertx.core.impl.cpu CpuCoreSensor)))

(defprotocol IDonkey
  (create-server [_this opts]
    "Create an instance of DonkeyServer with the supplied options.
    `opts` map description (all values are optional unless stated otherwise):

    :port [int] Required. The port the server will listen to.

    :routes [map [,map]*] Sequence of routes that the server should handle.
      All values are optional unless stated otherwise:

      - :handler [fn] Required. A function that accepts 1 or 3 arguments
          depending on the value of `:handler-mode`. The function will be called
          if a request matches the route. When `:handler-mode` is
          `:non-blocking` (the default value) the handler must accept 3
          arguments - a request map, respond function, and raise function. When
          `:handler-mode` is `:blocking` the handler must accept a single
          argument - a request map.

      - :handler-mode [keyword] `:blocking` or `:non-blocking`. See `:handler`
          description for usage. Defaults to `:non-blocking`.

      - :path [string] Used in matching a request to a route. The path is the
          first element that's examined when matching a request to a route. It is
          the part of the URI that comes after the hostname, and identifies a
          resource on the host the client is trying to access. The path is
          matched based on the value of `:match-type`. Defaults to any path.

      - :match-type [keyword] `:simple` or `:regex`. `:simple` will look for an
          exact match on the string value of `:path`, or a path variable match
          for `path` values that follow this pattern: `/(?::[a-zA-Z0-9]+/?)+`.
          `:regex` will do a regular expression match with the value of `path`.
          Defaults to `:simple`

      - :methods [keyword [,keyword]*] Sequence of HTTP methods (verbs) that
          this route accepts. Possible values are the method name (e.g `GET`) in
          lowercase (e.g `:get`). Defaults to any method.

      - :consumes [string [,string]*] Sequence of MIME types that this route
          accepts. For example, `application/json`, `application/octet-stream`,
          or `multipart/mixed`. The value will be matched against the request's
          `Content-Type` header. If the route matches but the content type does
          not, then a `415 Unsupported Media Type` response will be returned.
          Defaults to any MIME type.

      - :produces [string [,string]*] Sequence of MIME types that this route
          produces. For example, `application/json`, `application/octet-stream`,
          or `text/html`. The value will be matched against the request's
          `Accept` header. If the route matches but the request does not accept
          any of the MIME types then a `406 Not Acceptable` response will be
          returned. Defaults to any MIME type.

      - :middleware [fn [,fn]*] Sequence of functions that will be applied
          before a request is processed by the handler. Each function should
          accept a handler function as its only argument, and return a function
          that accepts 1 or 3 arguments (blocking vs. non blocking mode).
          The middleware is responsible calling the handler before or after the
          handler processes the request.

    :instances [int] The number of server instances to deploy. Each instance
      is assigned to an event loop. Therefore there is a direct relation between
      the number of :event-loops set when creating a Donkey instance, and the
      number of server instances. If the number of server instances is greater
      than the number of event loops then some will have to share.
      Also note that the function returns a *single* DonkeyServer. The number
      of instances only determines the amount of concurrency that will be used
      to serve requests.

    :middleware [fn [,fn]*] Sequence of functions that will be applied before
      a request is processed by a handler. Each function should accept a handler
      function as its only argument, and return a function that accepts 1 or 3
      arguments (blocking vs. non blocking mode). The middleware is responsible
      calling the handler before or after the handler processes the request.

    :compression [boolean] Include support for gzip  request / response inflate /
      deflate. Defaults to false.

    :host [string] The hostname or ip the server will listen to incoming
      connections. Defaults to '0.0.0.0'.

    :idle-timeout-seconds [int] Set the duration of time in seconds where a
      connection will timeout and be closed if no data is received. Defaults to
      never.

    :debug [boolean] Enable debug mode. Debug mode is not suitable for
      production use since it outputs a large amount of logs. Use with
      discretion. Defaults to false.

    :date-header [boolean] Include the 'Date' header in the response. Defaults
      to false.

    :server-header [boolean] Include the 'Server' header in the response.
      Defaults to false.

    :content-type-header [boolean] Sets the response content type automatically
      according to the best 'Accept' header match. Defaults to false.
    ")

  (create-client [_this] [_this opts]
    "Create an instance of DonkeyClient with the supplied options.
    `opts` map description (all values are optional unless stated otherwise):

    :default-host [string] The default host to use for all requests. Can be
      overridden on per request basis.

    :default-port [int] The default port to use for all requests. Can be
      overridden on per request basis.

    :ssl [boolean] Enable SSL handling for all requests. Can be overridden on
      per request basis.

    :debug [boolean] Enable debug mode. Debug mode is not suitable for
      production use since it outputs a large amount of logs. Use with
      discretion. Defaults to false.

    :compression [boolean] Enable client side compression. Defaults to false.

    :force-sni [boolean] Enable support for Server-Name-Indication. When an SSL
      connection is attempted, the client will send the hostname it is
      attempting to connect to during the handshake process. Defaults to true.

    :keep-alive [boolean] Enable keep alive connections. When enabled multiple
      requests will be transmitted on the same connection rather than opening
      and closing a connection for each request. It is recommended to use this
      option when it is known that multiple consecutive requests will be made
      to the same host. The amount of time a connection is kept alive can be
      configured with :keep-alive-timeout-seconds. Defaults to false.

    :keep-alive-timeout-seconds [int] The duration of seconds to keep a
      connection alive. Ignored if :keep-alive is false. Defaults to 60 seconds.

    :connect-timeout-seconds [int] The duration of seconds to allow for a
      connection to be established. Defaults to 60 seconds.

    :idle-timeout-seconds [int] The duration of seconds after which the
      connection will be closed if no data was received. Can be overridden on
      per request basis. Defaults to never.

    :max-redirects [int] The maximum number of times to follow 3xx redirects.
      Defaults to 16.

    :user-agent-enabled [boolean] Indicates whether to include a 'User-Agent'
      header in the request. Defaults to false.

    :user-agent [string] The value of the 'User-Agent' header sent in the
      request. Ignored if :user-agent-enabled is set to false. Defaults to
      'Donkey-Client'.

    :proxy-options [map] Options for connecting to a proxy client. All values
      are required.
      - :host [string] The host to connect to.
      - :port [int] The port to connect to.
      - :proxy-type [keyword] :http, :socks4, or :socks5
    "))

(deftype Donkey [^Vertx vertx]
  IDonkey
  (create-server [_this opts]
    (-> (spec/assert ::donkey-spec/server-config opts)
        (assoc :vertx vertx)
        server/map->ServerConfig
        Server/create
        server/->DonkeyServer))
  (create-client [this]
    (create-client this {}))
  (create-client [_this opts]
    (-> (spec/assert ::donkey-spec/client-config opts)
        (assoc :vertx vertx)
        client/map->ClientConfig
        RingClient/create
        client/->DonkeyClient)))

(defn- ^VertxOptions map->VertxOptions
  "Creates and returns a VertxOptions object from the opts map.
  The vertx options are used to initialize the Vertx object which is an
  integral part of the server and client. It allows configuring thread pools
  and metrics."
  [opts]
  (let [vertx-options (VertxOptions.)]
    (.setPreferNativeTransport vertx-options true)
    (.setEventLoopPoolSize vertx-options (int (:event-loops opts (CpuCoreSensor/availableProcessors))))
    (when-let [worker-threads (:worker-threads opts)]
      (.setWorkerPoolSize vertx-options (int worker-threads)))
    (when (:metric-registry opts)
      (.setMetricsOptions vertx-options (metrics/map->MetricsOptions opts)))
    vertx-options))

(defn ^Donkey create-donkey
  "Create a Donkey factory. Use the factory to create an HTTP server or client.
  `opts` map description (all values are optional unless stated otherwise):

  :event-loops [int] The number of event loops that will be used by this
    instance. An event loop corresponds to a single OS thread. Every server and
    client created by this Donkey instance will share its event loops. It is
    recommended to have at least one event loop per available CPU core, which is
    also the default setting.

  :worker-threads [int] The number of worker threads that will be used when
    :handler-mode is :blocking. In blocking mode all user code will be executed
    off the event loop by a worker thread. It is not recommended to run blocking
    handlers, unless absolutely necessary. It is necessary to experiment
    with the size of :worker-threads until you reach the desired application
    requirements.

  :metrics-prefix [string] A prefix that will be added to all metrics. Can be used
    to differentiate between different projects. Defaults to 'donkey'.

  :metric-registry [MetricRegistry] Instance of Dropwizard MetricRegistry where
    metrics will be reported to.
  "
  ([] (create-donkey {}))
  ([opts]
   (-> (spec/assert ::donkey-spec/donkey-config opts)
       map->VertxOptions
       Vertx/vertx
       ->Donkey)))
