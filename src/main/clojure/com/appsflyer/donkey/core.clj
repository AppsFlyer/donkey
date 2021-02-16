;
; Copyright 2020 AppsFlyer
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

(ns com.appsflyer.donkey.core
  (:require [clojure.set]
            [clojure.spec.alpha :as spec]
            [com.appsflyer.donkey.server :as server]
            [com.appsflyer.donkey.client :as client]
            [com.appsflyer.donkey.metrics :as metrics]
            [com.appsflyer.donkey.donkey-spec :as donkey-spec])
  (:import (com.appsflyer.donkey.server ServerImpl)
           (com.appsflyer.donkey.client.ring RingClient)
           (io.vertx.core VertxOptions Vertx)
           (io.vertx.core.impl.cpu CpuCoreSensor)
           (com.appsflyer.donkey VertxFactory FutureResult)
           (com.appsflyer.donkey.util DebugUtil)))

(spec/check-asserts true)

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
          arguments - a `request` map, `respond` function, and `raise` function.
          The handler must call the `respond` function with a Ring response map,
          or the `raise` function with an exception.
          When `:handler-mode` is `:blocking` the handler must accept a single
          argument - a `request` map. It should return a Ring response map.

      - :handler-mode [keyword=:non-blocking] `:blocking` or `:non-blocking`.
          See `:handler` description for usage.

      - :path [string] Used in matching a request to a route. The path is the
          first element that's examined when matching a request to a route. It is
          the part of the URI that comes after the hostname, and identifies a
          resource on the host the client is trying to access. The path is
          matched based on the value of `:match-type`. Defaults to any path.

      - :match-type [keyword=:simple] `:simple` or `:regex`. `:simple` will look
          for an exact match on the string value of `:path`, or a path variable
          match for `path` values that follow this pattern:
          `/(?::[a-zA-Z0-9]+/?)+`. `:regex` will do a regular expression match
          with the value of `path`.

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
          before a request is processed by the handler. The middleware provided
          during server initialization (if any) takes precedence over this
          middleware. In other words, it is called before the route specific
          middleware. Each function should accept a handler function as its only
          argument, and return a function that accepts 1 or 3 arguments
          (blocking vs. non blocking mode). The middleware is responsible
          calling the handler before or after the handler processes the request.

    :error-handlers [map] A map of HTTP status code to handler function.
      Provides a mechanism for users to handle unexpected errors such as 4xx
      and 5xx that are not handled by a route handler. Examples:
      - An uncaught exception inside a route handler will trigger the 500 status
       code function.
      - When no request can be matched to a route then the 404 status code
       function will be called.
      - Conversely, when a route handler returns a response with 4xx or 5xx
       error status code, then no error handler will be called.
      The function will be called with a map with the following fields:
        - path [string] The path on which the error occurred.
        - cause [Throwable] The exception that caused the error if there is one.
      It should return a Ring response map

    :instances [int] The number of server instances to deploy. Each instance
      is assigned to an event loop. Therefore there is a direct relation between
      the number of :event-loops set when creating a Donkey instance, and the
      number of server instances. If the number of server instances is greater
      than the number of event loops then some will have to share.
      Also note that the function returns a *single* DonkeyServer. The number
      of instances only determines the amount of concurrency that will be used
      to serve requests.

    :middleware [fn [,fn]*] Sequence of functions that will be applied before
      a request is processed by a handler. This middleware takes precedence over
      route specific middleware (if any). In other words, it is called before the
      latter. Each function should accept a handler function as its only
      argument, and return a function that accepts 1 or 3 arguments (blocking
      vs. non blocking mode). The middleware is responsible calling the handler
      before or after the handler processes the request.

    :compression [boolean=true] Include support for gzip response deflation.

    :decompression [boolean=true] Include support for gzip request inflation.

    :host [string='0.0.0.0'] The hostname or ip the server will listen to
      incoming connections.

    :idle-timeout-seconds [int=0] Set the duration of time in seconds where a
      connection will timeout and be closed if no data is received.
      Defaults to forever.

    :keep-alive [boolean=false] Enable keep alive connections. When enabled
      multiple requests will be transmitted on the same connection rather than
      opening and closing a connection for each request. It is recommended to
      use this option when it is known that multiple consecutive requests will
      be made from the same client.

    :tcp-no-delay [boolean=true] Determines whether packets are sent to the
      client as soon as they are available, even if there is only a small amount
      of data.
      i.e. `true` disables Nagle's Algorithm, and `false` enables it.
      See: https://en.wikipedia.org/wiki/Nagle%27s_algorithm

    :tcp-quick-ack [boolean=false] Determines whether an ACK is sent for every
      tcp segment immediately as it is received rather than delayed if needed in
      accordance to normal TCP operation.
      i.e. `true` disables Delayed ACK, and `false` enables it.
      Only available on Linux transport.

    :tcp-fast-open [boolean=false] Determines whether data can be carried in the
      SYN and SYN-ACK packets during initial connection handshake to save on
      full-round trip time.
      See: https://tools.ietf.org/html/rfc7413
      Only available on Linux transport.

    :socket-linger-seconds [int] Enable SO_LINGER with the specified linger time
      in seconds. The maximum timeout value is platform specific.
      The setting only affects socket close. Disabled by default.

    :accept-backlog [int=1024] The maximum queue length for incoming connection
      indications (a request to connect). If a connection indication arrives
      when the queue is full, the connection is refused.

    :date-header [boolean=false] Include the 'Date' header in the response.

    :server-header [boolean=false] Include the 'Server' header in the response.
      Defaults to false.

    :content-type-header [boolean=false] Sets the response content type
      automatically according to the best 'Accept' header match.
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

    :compression [boolean=false] Enable client side compression.

    :force-sni [boolean=true] Enable support for Server-Name-Indication.
      When an SSL connection is attempted, the client will send the hostname it
      is attempting to connect to during the handshake process.

    :keep-alive [boolean=false] Enable keep alive connections. When enabled
      multiple requests will be transmitted on the same connection rather than
      opening and closing a connection for each request. It is recommended to
      use this option when it is known that multiple consecutive requests will
      be made to the same host. The amount of time a connection is kept alive
      can be configured with :keep-alive-timeout-seconds.

    :keep-alive-timeout-seconds [int=60] The duration of seconds to keep a
      connection alive. Ignored if :keep-alive is false.

    :connect-timeout-seconds [int=60] The duration of seconds to allow for a
      connection to be established.

    :idle-timeout-seconds [int=0] The duration of seconds after which the
      connection will be closed if no data was received. Can be overridden on
      per request basis. Defaults to forever.

    :max-redirects [int=16] The maximum number of times to follow 3xx redirects.

    :user-agent-enabled [boolean=false] Indicates whether to include a 'User-Agent'
      header in the request.

    :user-agent [string=Donkey-Client] The value of the 'User-Agent' header sent
      in the request. Ignored if :user-agent-enabled is set to false.

    :proxy-options [map] Options for connecting to a proxy client. All values
      are required.
      - :host [string] The host to connect to.
      - :port [int] The port to connect to.
      - :proxy-type [keyword] :http, :socks4, or :socks5
    ")
  (destroy [this]
    "Releases all the underlining resources associated with this instance.
    Servers and clients that were created with this instance cannot be used
    afterwards. It is equivalent to calling `stop` on any existing server
    or client instances. The call is asynchronous and returns a `FutureResult`"))

;; config is a map with the following keys:
;; :vertx [Vertx] The underlining Vertx instance
;; :debug [boolean=false] Whether to run in debug mode
(deftype Donkey [config]
  IDonkey
  (create-server [this opts]
    (-> (spec/assert ::donkey-spec/server-config opts)
        (merge (.-config this))
        server/map->ServerConfig
        ServerImpl/create
        server/->DonkeyServer))
  (create-client [this]
    (create-client this {}))
  (create-client [this opts]
    (-> (spec/assert ::donkey-spec/client-config opts)
        (merge (.-config this))
        client/map->ClientConfig
        RingClient/create
        client/->DonkeyClient))
  (destroy [this]
    (let [vertx ^Vertx (-> this .-config :vertx)]
      (FutureResult/create (.close vertx)))))

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

  :worker-threads [int=20] The number of worker threads that will be used when
    :handler-mode is :blocking. In blocking mode all user code will be executed
    off the event loop by a worker thread. It is not recommended to run blocking
    handlers, unless absolutely necessary. It is necessary to experiment
    with the size of :worker-threads until you reach the desired application
    requirements.

  :metrics-prefix [string='donkey'] A prefix that will be added to all metrics. Can be used
    to differentiate between different projects.

  :metric-registry [MetricRegistry] Instance of Dropwizard MetricRegistry where
    metrics will be reported to.

  :debug [boolean=false] Enable debug mode. Debug mode is not suitable for production
    use since it outputs a large amount of logs. Use with discretion.
  "
  ([] (create-donkey {}))
  ([opts]
   (when-let [opts (spec/assert ::donkey-spec/donkey-config opts)]
     ; We need to initialize debug logging before a Logger
     ; is created, so SLF4J will use Logback instead of another provider.
     (if (true? (:debug opts)) (DebugUtil/enable) (DebugUtil/disable))

     (->Donkey
       (assoc
         (select-keys opts [:debug])
         :vertx
         (-> opts map->VertxOptions VertxFactory/create))))))
