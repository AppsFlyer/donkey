# Donkey


## TODO
- Consider removing the JMX support for metrics - make it the user problem.
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
- SSL support on the server
- HTTP2


## Donkey

##### Creating a Donkey

In Donkey, you create HTTP servers and clients using a - `Donkey`.
Creating a `Donkey` is simple:
```clojure
(ns com.appsflyer.sample-app
  (:require [com.appsflyer.donkey.core :refer [create-donkey]]))

  (def ^Donkey donkey-core (create-donkey))
```  

We can also configure our donkey instance:
```clojure
(ns com.appsflyer.sample-app
  (:require [com.appsflyer.donkey.core :refer [create-donkey]]))

  (def donkey-core (create-donkey {:event-loops     4
                                   :metrics-enabled true
                                   :metrics-prefix  "sample-app"}))
```
There should only be a single `Donkey` instance per application. That's because 
the client and server will share the same resources making them very efficient.
`Donkey` is a factory for creating server(s) and client(s) (you _can_ create multiple 
servers and clients with a `Donkey`, but in almost all cases you will only want 
a single server and / or client per application).

## Server

##### Creating a Server

Creating a server is done using a `Donkey` instance. For example, this is how 
you would create a simple server listening for requests on port 8080.
```clojure
(ns com.appsflyer.sample-app
  (:require [com.appsflyer.donkey.core :refer [create-donkey create-server]]
            [com.appsflyer.donkey.server :refer [start]]))

  (let [donkey-core (create-donkey)]
    (->         
      (create-server donkey-core {:port 8080}))
      start
      (on-success (fn [_] (println "Server started listening on port 8080"))))
``` 
Note that the call to `start` is asynchronous and therefore will return before
the server actually started listening for incoming connections. It's possible
to block the current thread execution until the server is running by calling 
`start-sync` or by derefing the arrow macro.

At this point the server won't actually do much, because we haven't defined any
[routes](). Let's define a route and create a basic "Hello world" server.

```clojure
(ns com.appsflyer.sample-app
  (:require [com.appsflyer.donkey.core :refer [create-donkey create-server]]
            [com.appsflyer.donkey.server :refer [start]]))

  (let [donkey-core (create-donkey)]
    (-> 
      (create-server donkey-core {:port   8080
                                  :routes [{:handler (fn [_req res _err] 
                                                       (res {:body "Hello, world!"}))}]}))
      start
      (on-success (fn [_] (println "Server started listening on port 8080"))))
``` 

As you can see we added a `:routes` key to the options map used to initialise 
the server. A route is just a map that describes what kind of requests are 
handled at a specific resource address (or `:path`), and how to handle them. The 
only required key is `:handler`, which will be called when a request matches a 
route. In the example above we're saying that we would like any request to be 
handled by our handler function. 

Our handler is a Ring compliant asynchronous handler. If you are not familiar 
with the [Ring](https://github.com/ring-clojure/ring/blob/master/SPEC) 
async handler specification, here's an excerpt:
>An asynchronous handler takes 3 arguments: a request map, a callback function
 for sending a response and a callback function for raising an exception. The
 response callback takes a response map as its argument. The exception callback
 takes an exception as its argument.
  
In the handler we are calling the response callback `res` with a response map
where the body of the response is "Hello, world!".

If you run the example and open a browser on `http://localhost:8080` you will
see a page with "Hello, World!".

##### Server options



#### Routes

In Donkey HTTP requests are routed to handlers. When you initialise a server
you define a set of routes that it should be able to handle. When a request 
arrives the server checks if one of the routes can handle the request. If no 
matching route is found, then a `404 Not Found` response is returned to the 
client.

Let's see a route example:
```clojure
{
  :handler      (fn [request respond raise] ...)
  :handler-mode :non-blocking
  :path         "/api/v2"
  :match-type   :simple
  :methods      [:get :put :post :delete]
  :consumes     ["application/json"]
  :produces     ["application/json"]
  :middleware   [(fn [handler] (fn [request respond raise] (handler request respond raise)))]
}
```   
`:handler` A function that accepts 1 or 3 arguments (depending on 
`:handler-mode`). The function will be called if a request matches the route. 
This is where you call your application code. 

`:handler-mode` To better understand the use of the `:handler-mode`, we need
to first get some background about Donkey. Donkey is an abstraction built on top 
of a web tool-kit called [Vert.x](https://vertx.io/), which in turn is built on
a very popular and performant networking library called 
[Netty](https://netty.io/). Netty uses an interesting threading model that is 
based on the concept of a single threaded event loop that serve requests. An 
event loop is conceptually a long-running task with a queue of events it needs 
to dispatch. As long as the events dispatch quickly and don't occupy too much of
the event loop's time, it can dispatch events at a very high rate. Because it
is single threaded, or in other words serial, during the time it takes to 
dispatch one event, no other event can be dispatched. Therefore, it's extremely 
important *not to block the event loop*.

The `:handler-mode` is a contract where you declare the type of handling your
route does - `:blocking` or `:non-blocking` (default). 
`:non-blocking` means
that the handler is either doing a very quick computational work, or that the
work will offloaded to a separate thread. In both cases the guarantee is that it
will *not block the event loop*. In this case the `:handler` must accept 3 
arguments.
Sometimes reality has it that we have to deal with legacy code that is doing
some blocking operations that we just cannot change easily. For these occasions
we have `:blocking` handler mode. In this case, the handler will be called on a 
separate worker thread pool without needing to worry about blocking the event 
loop. The worker thread pool size can be configured when creating a 
[`Donkey`](#creating-a-donkey) instance by setting the `:worker-threads` option.

`:path` is the first thing a route is matched on. It is the part after the 
hostname in a URI that identifies a resource on the host the client is trying to 
access. The way the path is matched depends on the `:match-type`.

`:match-type` can be either `:simple` or `:regex`.

`:simple` match type will match in two ways:
1. Exact match. In the example above it means the route will only match requests 
to `http://localhost:8080/api/v2`. It will _not_ match requests to: 
- `http://localhost:8080/api` 
- `http://localhost:8080/api/v3` 
- `http://localhost:8080/api/v2/user`
2. Path variables. Take for example the path `/api/v2/user/:id/address`. `:id`
is a path variable that matches on any sub-path. All the following paths
will match:
- `/api/v2/user/1035/address`   
- `/api/v2/user/2/address`   
- `/api/v2/user/foo/address`   
The really nice thing about path variables is that you get the value that was in
the path when it matched, in the request. The value will be available in the 
`:path-params` map. If we take the first example, the request will look like 
this:
```clojure
{
... regular request fields
  :path-params {"id" "1035"}
... more request fields 
}
```        
`:regex` match type will match on arbitrary regular expressions. For example, if
wanted to only match the `/api/v2/user/:id/address` path if `:id` is a number,
then we could use `:match-type :regex` and supply this path:
`/api/v2/user/[0-9]+/address`. In this case the route will only match if a 
client requests the path with a numeric id, but we won't have access to the id
in the `:path-params` map. If we wanted the id we could fix it by adding 
capturing groups: `/api/v2/user/([0-9]+)/address`. Now everything within the 
parenthesis will be available in `:path-params`. 
```clojure
{
  :path-params {"param0" "1035"}
}
```
We can also add multiple capturing groups, for example the path
`/api/v(\d+\.\d{1})/user/([0-9]+)/address` will match `/api/v4.7/user/9/address`
and `:path-params` will include both capturing groups.
```clojure
{
  :path-params {"param0" "4.7" 
                "param1" "9"}
}
```

`:methods` is a vector of HTTP methods the route supports, such as GET, POST, 
etc'. By default, any method will match the route.

`:consumes` is a vector of media types that the handler can consume. If a route
matches but the `Content-Type` header of the request doesn't match one of the
supported media types, then the request will be rejected with a 
`415 Unsupported Media Type` code.

`:produces` is a vector of media types that the handler produces. If a route
matches but the `Accept` header of the request doesn't match one of the
supported media types, then the request will be rejected with a 
`406 Not Acceptable` code.  

`:middleware` is a vector of [middleware](#middleware) functions that will be
applied to the route. It is also possible to supply a "global" 
`:middleware` vector when [creating a server](#creating-a-server) that will be 
applied to all the routes. In that case the global middleware will be applied 
*first*, followed by the middleware specific to the route.  




- Using reitit
- Using compojure
- Simplest GET request
- GET request with parameters
- POST request with raw body
- POST request urlencoded
- POST request multipart with file upload


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
and any optional arguments required by the middleware. A `handler` in this 
context can be either another middleware, or a [route](#routes) handler.
The higher-order function should return a function that accepts one or three arguments:
- One argument: Called when `:handler-mode` is `:blocking` with a `request` map.
- Three arguments: Called when `:handler-mode` is `:non-blocking` with a 
`request` map, `respond` function, and `raise` function. The `respond` function 
should be called with the result of the next handler, and the `raise` function 
should be called when it is impossible to continue processing the request 
because of an exception.
 
The `handler` argument that was given to the higher-order function has the same 
signature as the function being returned. It is the middleware author's 
responsibility to call the next `handler` at some point.   
 
Here's an example of a one argument middleware adding a timestamp to a request:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request] 
    (handler 
      (assoc request :timestamp (System/currentTimeMillis)))))
```

Here's an example of the same middleware with three arguments:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request respond raise]
    (try
      (handler
        (assoc request :timestamp (System/currentTimeMillis)) respond raise)
      (catch Exception ex
        (raise ex)))))
```

In the last couple of examples we've been updating the request and calling
the next handler with the transformed request. Middleware is not limited to
only processing and transforming the request. Here is an example of a three 
argument middleware that adds a `Content-Type` header to the _response_.
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

As mentioned before, the three argument function is called when the 
`:handler-mode` is `:non-blocking`. Notice that we are doing the processing on 
the calling thread - the event loop. That's because the overhead of 
[context switching](https://www.tutorialspoint.com/what-is-context-switching-in-operating-system), 
and potentially spawning a new thread by offloading a simple `assoc` 
or `update` to a separate thread pool would greatly outweigh the processing time
on the event loop. However, if for example we had a middleware that 
performs some operation on a remote database, then we would need to run it on a 
separate thread.  

In this example we authenticate a user with a remote service. For the sake of 
the example, all we need to know is that we get back a 
[CompletableFuture](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
that is executed on a different thread. When the future completes, we check
if we had an exception, and then either call the next `handler` with the updated
request, or stop the execution by calling `raise`.
```clojure
(defn user-authentication-middleware [handler]
  (fn [request respond raise]
    (.whenComplete
      ^CompletableFuture (authenticate-user request)
      (reify BiConsumer
        (accept [this result exception]
          (if (nil? exception)
            (handler (assoc request :authenticated result) respond raise)
            (raise exception)))))))
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
As later described, metrics are named using a `.` as a separator. By default, all metrics 
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

#### Submitting a Request

Calling `(def async-request (request donkey-client opts))` creates an 
`AsyncRequest` but does not submit the request yet. You can reuse an 
`AsyncRequest` instance to make the same request multiple times. There are 
several ways a request can be submitted:

- `(submit async-request)` submits a request without a body. This is usually 
the case when doing a `GET` request.
- `(submit async-request body)` submits a request with a raw body. `body` can
be either a string, or a byte array. A typical use case would be `POST`ing
serialized data such as JSON. Another common use case is sending binary data 
by also adding a `Content-Type: application/octet-stream` header to the request. 
- `(submit-form async-request body)` submits an urlencoded form. A 
`Content-Type: application/x-www-form-urlencoded` header will automatically be 
added to the request, and the body will be urlencoded. `body` is a map of string
key-value pairs. 
For example, this is how you would typically submit a sign in form on a website:
```clojure
(submit-form async-request {"email"    "frankies15@example.com" 
                            "password" "only-on-ssl"})
```
- `(submit-multipart-form async-request body)` submits a multipart form. A 
`Content-Type: multipart/form-data` header will automatically be added to the 
request. Multipart forms can be used to send simple key-value attribute pairs, 
and uploading files. For example, you can upload a file from the filesystem along
with some attributes like this:
```clojure
(submit-multipart-form 
  async-request 
    {"Lyrics"     "Phil Silvers"
     "Music"      "Jimmy Van Heusen"
     "Title"      "Nancy (with the Laughing Face)"
     "Media Type" "MP3"
     "Media"      {
                   "filename"       "nancy.mp3"
                   "pathname"       "/home/bill/Music/Sinatra/Best of Columbia/nancy.mp3"
                   "media-type"     "audio/mpeg"
                   "upload-as"      "binary" 
                  }
   })
```  

#### FutureResult

Requests are submitted asynchronously, meaning the request is executed on 
a background thread, and calls to `submit[-xxx]*` return a `FutureResult` 
immediately. You can think of a `FutureResult` as a way to subscribe to an event
that may have happened or will happen some time in the future. The api is very 
simple:
- `(on-success async-result (fn [result]))` will call the supplied function
with a response map from the server, iff there were no client side errors while 
executing the request. Client side errors include an unhandled exception, or 
problems connecting with the server. It does not include server errors such as 
4xx or 5xx response status codes. The response will have the usual Ring fields - 
`:status`, `:body`, and optional `:headers`.
- `(on-fail async-result (fn [ex]))` will call the supplied function
with an `ExceptionInfo` indicating the request failed due to a client error.
- `(on-complete async-result (fn [result ex]))` will always call the
supplied function whether the request was successful or not. A successful 
request will be called with `ex` being `nil`, and a failed request will
be called with `result` being `nil`. The two are mutually exclusive which makes
it simple to check the outcome of the request.

It's possible for multiple parties to be notified on the completion of 
`FutureResult` - each of the `on-success`, `on-fail`, and `on-complete` can be
called zero or more times. If the response is irrelevant as is the case in "call 
and forget" type requests, then the result can be ignored:
```clojure
(submit async-request) ; => The `FutureResult` returned is ignored
... do the rest of your application logic
```

Or if you are only interested to know if the request failed:
```clojure
(-> 
  (submit async-request)
  (on-fail (fn [ex] (println (str "Oh, no. That was not expected - " (ex-message ex)))))
... do the rest of your application logic
```

Although it is not recommended, results can also be derefed:
```clojure
(let [result @(submit async-request)]
  (if (map? result)
    (println "Yea!")
    (println "Nay :(")))
``` 
In this case the call to `submit` will block the calling thread until a result
is available. The result may be either a response map, if the request was 
successful, or an `ExceptionInfo` if it wasn't.            


- Simplest GET request
- GET request with parameters
- POST request with raw body
- POST request urlencoded
- POST request multipart with file upload
- Overriding host + port
- Proxy request
- Basic authentication
- Bearer token (OAuth2)


The rest of the examples assume the following vars are defined
 
```clojure
(def donkey-core (donkey/create-donkey))
(def donkey-client (donkey/create-client donkey-core)
```  
 
#### HTTPS Requests

Making HTTPS requests requires only setting `:ssl` to `true` when creating the 
client or the request. The port can be omitted and will default to 443. If 
you've already set a default-port when creating the client, then you must 
override it when creating the request.

```clojure
(->
  (request donkey-client {:host   "reqres.in"
                          :port   443
                          :ssl    true
                          :uri    "/api/users?page=2"
                          :method :get})
  submit
  (on-success (fn [res] (println res)))
  (on-fail (fn [ex] (println ex))))

 ;  Will output something like this:
 ; `{:status 200, 
     :headers {Age 365, Access-Control-Allow-Origin *, CF-Cache-Status HIT, Via 1.1 vegur, Set-Cookie __cfduid=deb7baeea854619ab27bf36abf222b4dc1599922248; expires=Mon, 12-Oct-20 14:50:48 GMT; path=/; domain=.reqres.in; HttpOnly; SameSite=Lax; Secure, Date Sat, 12 Sep 2020 14:50:48 GMT, Accept-Ranges bytes, cf-request-id 05246533bf0000ad73b62fa200000001, Expect-CT max-age=604800, report-uri="https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct", Cache-Control max-age=14400, Content-Length 1245, Server cloudflare, Content-Type application/json; charset=utf-8, Connection keep-alive, Etag W/"4dd-IPv5LdOOb6s5S9E3i59wBCJ1k/0", X-Powered-By Express, CF-RAY 5d1a7165fa2cad73-TLV}, 
     :body #object[[B 0x7be7d50c [B@7be7d50c]}`

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
