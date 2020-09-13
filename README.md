# Donkey


## TODO
- Add routing benchmarks to compare with reitit 
- Look at response validation (expectation)
https://vertx.io/docs/vertx-web-client/java/#response-predicates
- Look into implementing JSON serialization / deserialization middleware
- Look into functional builder pattern rather than using a configuration map.
Examples:
https://dave.cheney.net/2014/10/17/functional-options-for-friendly-apis
https://github.com/AppsFlyer/unleash-client-clojure/blob/master/test/unleash_client_clojure/builder_test.clj
- Go over donkey-spec with Ben, work on dependant types.  
- See about adding health check support.
It's probably going to be simple enough to create an "alive" health check
without user interference (or with minimal such as port and uri).
A "healthy" health check would require the user to provide a handler. 
- Clean up the middleware code in route.clj
- Add more middleware tests
- TESTS!
- Documentation
- Examples
- README
- Setup CI

========================================

## DONE
- Client SSL support
- Client implementation
- Add metrics documentation
- Explore having the metrics implementation pluggable by the user.
This is a feature that can be added at a later stage. It's not required in the near future.
- Consider going completely data oriented. The config is just json, and the handlers 
are namespaced keywords point to user functions.
Spoke with Doron about it, and we both agreed we don't see the benefits in going this route.
It can be further discussed in the future. 
- Check about using OpenCensus rather than DropWizard for metrics
From my understanding we can use Dropwizard to collect the metrics because it is 
integrated with Vert.x. There is no integration with OpenCensus, but we can still use it
in order to report the metrics. That can be done by an external library and should not be
part of the server code.
- Turn off / on vertx features in production / debug mode.
These features have to be turned on / off by system properties on the command line
when the application starts. Otherwise, there's the possibility of mistakenly overriding
the user's intentions. I added placeholders for the documentation about available options.
- Remove leak detector in production and add in debug mode
The leak detector is "disabled" by vertx in VertxImpl when the class loads.
It says it's not needed in vertx, but I turn it to "simple" mode when :debug is true  
- Implement debug mode
- Make adding content-type and server headers optional
- Make adding the "Date" header optional
- Refactor middleware to comply with Ring
- Think about short circuit of handlers
- Implement global middleware
- Add support for per route middleware 
- Metrics are not working
- Use Spec 'assert' instead of 'conform'.
- Path variables are not working - 404
- Make number of event loops configurable


## Future releases
- SSL support

## Server

## Usage

Blocking handler mode. 
```clojure
(-> {:port   8080
     :routes [{:path         "/hello-world"
               :methods      [:get]
               :handler-mode :blocking
               :consumes     ["text/plain"]
               :handler     (fn [_req]
                                {:status  200
                                 :headers {"content-type" "application/json"}
                                 :body    (.getBytes "{\"greet\":\"Hello world!\"}")})}]}
    donkey/create-server
    server/start)
```

Non-blocking handler mode.
```clojure
(-> {:port   8080
     :routes [{:path            "/greet/:name"
               :methods         [:get]
               :metrics-enabled true
               :consumes        ["text/plain"]
               :handler        (fn [req respond _raise]
                                   (future
                                     (respond
                                       {:status  200
                                        :headers {"content-type" "text/plain"}
                                        :body    (.getBytes
                                                   (str "Hello " (-> :path-params req (get "name"))))})))}]}
    donkey/create-server
    server/start)
```

## Middleware

The term "middleware" is generally used in the context of HTTP frameworks
as a pluggable unit of functionality that can examine or manipulate the flow of bytes
between a client and a server. In other words, it allows users to do things such as 
logging, compression, validation, authorization, and transformation (to name a few) 
of requests and responses.

According to the [Ring](https://github.com/ring-clojure/ring/wiki/Concepts#middleware) 
specification, middleware are implemented as [higher-order functions](https://clojure.org/guides/higher_order_functions)
that accept one or more arguments, where the first argument is the next `handler` function, 
and any optional arguments required by the middleware. 
The higher-order function should return a function that accepts one or three arguments:
- One argument: Called with a `request` map argument when `:handler-mode` is `:blocking`.
- Three arguments: Called with a `request` map, `respond` function, and `raise` function, 
when `:handler-mode` is `:non-blocking`. The `respond` function should be called with the
result of the next handler, and the `raise` function should be called when an exception is
caught, and it is impossible to continue processing the request.
 
The `handler` argument the higher-order function received has the same signature as the returned function.
It is the middleware author's responsibility to call the next `handler` at some point.   
 
Here's an example of a one argument arity middleware adding timestamp to the request:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request] 
    (handler 
      (assoc request :timestamp (System/currentTimeMillis)))))
```

Here's an example of the same middleware with three argument arity:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request respond raise]
    (try
      (handler
        (assoc request :timestamp (System/currentTimeMillis)) respond raise)
      (catch Exception ex
        (raise ex)))))
```

Finally, here is an example of a three argument arity middleware that adds 
a `Content-Type` header to the _response_.
```clojure
(defn add-content-type-middleware [handler]
  (fn [request respond raise]
    (let [respond' (fn [response]
                     (try
                       (respond
                         (update response :headers assoc "Content-Type" "text/plain"))
                       (catch Exception ex
                         (raise ex))))]
        
      (handler request respond' raise))))
```

As mentioned before, the three argument arity function is called when the `:handler-mode`
is `:non-blocking`. Notice that we are doing the processing on the calling thread. 
That's because there would be no benefit in off loading a simple `assoc` or `update` 
to a separate thread. If for example we had a middleware that authenticates with a 
remote database then we should run it on a separate thread.  

In this example we authenticate a user with a remote service. We get back a
[CompletableFuture](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
that is executed on a different thread. When the future is complete, we check
if we had an exception, and then either call the next `handler` with the updated
request, or stop the execution by calling `raise`.
```clojure
(defn user-authentication-middleware [handler]
  (fn [request respond raise]
    (let [authentication-future ^CompletableFuture (authenticate-user request)]
     (.whenComplete
       authentication-future
       (reify BiConsumer
         (accept [this result exception]
           (if (nil? exception)
             (handler (assoc request :authenticated result) respond raise)
             (raise exception))))))))
```

## Metrics

The library uses Dropwizard to capture different metrics. The metrics can be largely 
grouped into three categories: 
- Thread Pool
- Server
- Client

Metrics collection can be enabled by setting `:metrics-enabled true` when creating
a server or client. By default, a new Dropwizard `MetricRegistry` is created and can be 
viewed as managed beans in profilers such as [VisualVM](https://visualvm.github.io/).
That might be enough for local development, but in production you'll want to have 
the metrics reported to some monitoring service such as [Prometheus](https://prometheus.io/), 
or [graphite](https://graphiteapp.org/).
A pre instantiated instance of `MetricRegistry` can be provided by setting `:metrc-registry instance`
in the configuration. 
As later described, metrics are named using a `.` as a separator. By default all metrics 
are prefixed with `donkey`, but it's possible to set `:metrics-prefix` to use a different string.    

### List of Exposed Metrics

#### Thread Pool Metrics   

Base name:  `<:metrics-prefix>`

- `event-loop-size` - A Gauge of the number of threads in the event loop pool
- `worker-pool-size` - A Gauge of the number of threads in the worker pool

Base name:  `<:metrics-prefix>.pools.worker.vert.x-worker-thread`
 
- `queue-delay` - A Timer measuring the duration of the delay to obtain the resource, i.e the wait time in the queue
- `queue-size` - A Counter of the actual number of waiters in the queue
- `usage` - A Timer measuring the duration of the usage of the resource
- `in-use` - A count of the actual number of resources used
- `pool-ratio` - A ratio Gauge of the in use resource / pool size
- `max-pool-size` - A Gauge of the max pool size
 

#### Server Metrics   

Base name: `<:metrics-prefix>.http.servers.<host>:<port>`

- `open-netsockets` - A Counter of the number of open net socket connections
- `open-netsockets.<remote-host>` - A Counter of the number of open net socket connections for a particular remote host
- `connections` - A Timer of a connection and the rate of its occurrence
- `exceptions` - A Counter of the number of exceptions
- `bytes-read` - A Histogram of the number of bytes read.
- `bytes-written` - A Histogram of the number of bytes written.
- `requests` - A Throughput Timer of a request and the rate of it’s occurrence
- `<http-method>-requests` - A Throughput Timer of a specific HTTP method request, and the rate of its occurrence
Examples: get-requests, post-requests
- `responses-1xx` - A ThroughputMeter of the 1xx response code
- `responses-2xx` - A ThroughputMeter of the 2xx response code
- `responses-3xx` - A ThroughputMeter of the 3xx response code
- `responses-4xx` - A ThroughputMeter of the 4xx response code
- `responses-5xx` - A ThroughputMeter of the 5xx response code

#### Client Metrics

Base name: `<:metrics-prefix>.http.clients`

- `open-netsockets` - A Counter of the number of open net socket connections
- `open-netsockets.<remote-host>` - A Counter of the number of open net socket connections for a particular remote host
- `connections` - A Timer of a connection and the rate of its occurrence
- `exceptions` - A Counter of the number of exceptions
- `bytes-read` - A Histogram of the number of bytes read.
- `bytes-written` - A Histogram of the number of bytes written.
- `connections.max-pool-size` - A Gauge of the max connection pool size
- `connections.pool-ratio` - A ratio Gauge of the open connections / max connection pool size
- `responses-1xx` - A Meter of the 1xx response code
- `responses-2xx` - A Meter of the 2xx response code
- `responses-3xx` - A Meter of the 3xx response code
- `responses-4xx` - A Meter of the 4xx response code
- `responses-5xx` - A Meter of the 5xx response code




   
## Debug mode
Debug mode is activated when creating the server with `:debug true`.
It will cause A LOT of logs to be written and therefore it is completely
unsuitable for production, and should only be used while debugging in development.
The logs include:
- Instantiation logs 
- Byte level traffic
- Request routing
- library debug logs   

  
## Logging
- Uses SLF4J
- For debug logging you need to have logback on your classpath.

## Start up options
JVM system properties that can be supplied when running the application
- `-Dvertx.threadChecks=false`: Disable blocked thread checks. Used by Vert.x to 
warn the user if an event loop or worker thread is being occupied above a certain 
threshold which will indicate the code should be examined. 
- `-Dvertx.disableContextTimings=true`: Disable timing context execution. These are 
used by the blocked thread checker. It does _**not**_ disable execution metrics that 
are exposed via JMX.  



## Client

## Usage

The following examples assume these required namespaces
```clojure
(:require [com.appsflyer.donkey.core :as donkey]
          [com.appsflyer.donkey.client :refer [request stop]]
          [com.appsflyer.donkey.result :refer [on-complete on-success on-fail]]
          [com.appsflyer.donkey.request :refer [submit submit-form submit-multipart-form]])
```

##### Creating a Client
Creating a client is as simple as this

```clojure
(let [donkey-core (donkey/create-donkey)
      donkey-client (donkey/create-client donkey-core)])
```

We can set up the client with some default options, so we won't need to supply
them on every request

```clojure
(let [donkey-core (donkey/create-donkey)
      donkey-client (donkey/create-client 
                      donkey-core 
                        {:default-host               "reqres.in"
                         :default-port               443
                         :debug                      true
                         :ssl                        true
                         :keep-alive                 true
                         :keep-alive-timeout-seconds 30
                         :connect-timeout-seconds    10
                         :idle-timeout-seconds       20
                         :enable-user-agent          true
                         :user-agent                 "ClientX-v24.4.98673"
                         :compression                true})]
    (-> donkey-client
        (request {:method :get
                  :uri    "/api/users"})
        submit
        (on-complete 
          (fn [res ex] 
            (println (if ex "Failed!" "Success!"))))))
```

The previous example made an HTTPS request to some REST api and printed out 
"Failed!" if an exception was received, or "Success!" if we got a response
from the server. We'll discuss how submitting requests and handling responses
work shortly.  

##### Stopping a Client

Once we're done with a client we should always stop it. This will release all 
the resources being held by the client, such as connections, event loops, etc'.
You should reuse a single client throughout the lifetime of the application,
and stop it only if it won't be used again. Once stopped it should not be used
again.
 
```clojure
(stop donkey-client)
```

---

The rest of the examples assume the following vars are defined

```clojure
(def donkey-core (donkey/create-donkey))
(def donkey-client (donkey/create-client donkey-core)
```  

- The FutureResult object - how to use.
- Simplest GET request
- GET request with parameters
- POST request with raw body
- POST request urlencoded
- POST request multipart with file upload
- Overriding host + port
- SSL request
- Proxy request
- Basic authentication
- Bearer token (OAuth2)
 
 

HTTPS request. Set `:ssl` to `true`. The port can be omitted and will default to
443 if you haven't set a default port for the client.


```clojure
(->
  (request donkey-client {:host   "reqres.in"
                          :port   443
                          :ssl    true
                          :uri    "/api/users?page=2"
                          :method :get})
  (submit)
  (on-success (fn [res] (println res)))
  (on-fail (fn [ex] (println ex))))

(comment
  Will output something like this:
  `{:status 200, :headers {Age 365, Access-Control-Allow-Origin *, CF-Cache-Status HIT, Via 1.1 vegur, Set-Cookie __cfduid=deb7baeea854619ab27bf36abf222b4dc1599922248; expires=Mon, 12-Oct-20 14:50:48 GMT; path=/; domain=.reqres.in; HttpOnly; SameSite=Lax; Secure, Date Sat, 12 Sep 2020 14:50:48 GMT, Accept-Ranges bytes, cf-request-id 05246533bf0000ad73b62fa200000001, Expect-CT max-age=604800, report-uri="https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct", Cache-Control max-age=14400, Content-Length 1245, Server cloudflare, Content-Type application/json; charset=utf-8, Connection keep-alive, Etag W/"4dd-IPv5LdOOb6s5S9E3i59wBCJ1k/0", X-Powered-By Express, CF-RAY 5d1a7165fa2cad73-TLV}, :body #object[[B 0x7be7d50c [B@7be7d50c]}`
)
```
   

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
