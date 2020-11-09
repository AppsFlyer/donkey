# Donkey

### WIP - ALPHA VERSION 

| Branch | Status | Coverage |
| ------ | ----- | -------- |
| master | [![Build Status](https://travis-ci.com/AppsFlyer/donkey.svg?token=zfFYSyWcTCemqZqHoxKt&branch=master)](https://travis-ci.com/AppsFlyer/donkey) | [![Coverage Status](https://coveralls.io/repos/github/AppsFlyer/donkey/badge.svg?branch=master)](https://coveralls.io/github/AppsFlyer/donkey?branch=master) |  
| donkey-0.1.0-alpha | [![Build Status](https://travis-ci.com/AppsFlyer/donkey.svg?token=zfFYSyWcTCemqZqHoxKt&branch=donkey-0.1.0-alpha)](https://travis-ci.com/AppsFlyer/donkey) | [![Coverage Status](https://coveralls.io/repos/github/AppsFlyer/donkey/badge.svg?branch=donkey-0.1.0-alpha)](https://coveralls.io/github/AppsFlyer/donkey?branch=donkey-0.1.0-alpha) |  

[![Clojars Project](https://img.shields.io/clojars/v/com.appsflyer/donkey.svg)](https://clojars.org/com.appsflyer/donkey)

Modern Clojure, Ring compliant, HTTP server and client, designed for ease of use 
and performance

Table of Contents
-----------------

* [Usage](#usage)
* [Requirements](#requirements)
* [Building](#building)
* [Start up options](#start-up-options)
* [Creating a Donkey](#creating-a-donkey)
* [Server](#server)
    * [Creating a Server](#creating-a-server)
    * [Routes](#routes)
    * [Support for Routing Libraries](#support-for-routing-libraries)
        * [reitit](#reitit)
        * [Compojure](#compojure)
    * [Middleware](#middleware)
        * [Overview](#overview)
        * [Examples](#examples)
        * [Common Middleware](#common-middleware)
    * [Examples](#server-examples)
* [Client](#client)
    * [Creating a Client](#creating-a-client)
    * [Stopping a Client](#stopping-a-client)
    * [Submitting a Request](#submitting-a-request)
    * [FutureResult](#futureresult)
    * [HTTPS Requests](#https-requests)
* [Metrics](#metrics)
    * [List of Exposed Metrics](#list-of-exposed-metrics)
        * [Thread Pool Metrics](#thread-pool-metrics)
        * [Server Metrics](#server-metrics)
        * [Client Metrics](#client-metrics)
* [Debug mode](#debug-mode)
    * [Logging](#logging)
* [Troubleshooting](#troubleshooting)
* [License](#license)

Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)

### Usage
Including the library in `project.clj`
```clojure
[com.appsflyer/donkey "0.1.0-SNAPSHOT"]
``` 

Including the library in `deps.edn`
```clojure
com.appsflyer/donkey {:mvn/version "0.1.0-SNAPSHOT"}
``` 

Including the library in `pom.xml`
```xml
<dependency>
    <groupId>com.appsflyer</groupId>
    <artifactId>donkey</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Requirements
- [Java](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html) 11+
- [Maven](http://maven.apache.org/download.cgi) 3.6.3+
- [Leiningen](https://leiningen.org/) 2.9.3+

### Building
The preferred way to build the project for local development is using 
Maven. It's also possible to generate an uberjar using Leiningen, but you 
**must** use Maven to install the library locally.

Note **IntelliJ IDEA** users:
There is a bug when running the Clojure tests via `clojure-maven-plugin` 
in IntelliJ's terminal, that doesn't happen when running them with Leiningen. 
If you are getting this error then run the tests from your OS terminal.
```
[ERROR] Failed to execute goal com.theoryinpractise:clojure-maven-plugin:1.8.3:test-with-junit (junit-tests) on project donkey: Clojure failed with exit value 2.: Process exited with an error: 2 (Exit value: 2) -> [Help 1]
org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal com.theoryinpractise:clojure-maven-plugin:1.8.3:test-with-junit (junit-tests) on project donkey: Clojure failed with exit value 2.
```  

Creating a jar with Maven
```shell script
mvn package
```

Creating an uberjar with Leiningen
```shell script
lein uberjar
```

Installing to a local repository
```shell script
mvn clean install
```   

## Start up options
JVM system properties that can be supplied when running the application
- `-Dvertx.threadChecks=false`: Disable blocked thread checks. Used by Vert.x to 
warn the user if an event loop or worker thread is being occupied above a certain 
threshold which will indicate the code should be examined. 
- `-Dvertx.disableContextTimings=true`: Disable timing context execution. These are 
used by the blocked thread checker. It does _**not**_ disable execution metrics that 
are exposed via JMX.  

## Creating a Donkey

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

  (def donkey-core (create-donkey {:event-loops 4}))
```
There should only be a single `Donkey` instance per application. That's because 
the client and server will share the same resources making them very efficient.
`Donkey` is a factory for creating server(s) and client(s) (you _can_ create multiple 
servers and clients with a `Donkey`, but in almost all cases you will only want 
a single server and / or client per application).

## Server

The following examples assume these required namespaces

```clojure
(:require [com.appsflyer.donkey.core :refer [create-donkey create-server]]
          [com.appsflyer.donkey.server :refer [start]]
          [com.appsflyer.donkey.result :refer [on-success]])
```

### Creating a Server

Creating a server is done using a `Donkey` instance. Let's start by creating 
a server listening for requests on port 8080. 
```clojure
(->         
  (create-donkey)
  (create-server {:port 8080})
  start
  (on-success (fn [_] (println "Server started listening on port 8080"))))
``` 
_Note that the following example will not work yet - for it to work we need
to add a route which we will do next._
 
After creating the server we `start` it, which is an asynchronous call that 
may return before the server actually started listening for incoming 
connections. It's possible to block the current thread execution until the 
server is running by calling `start-sync` or by "derefing" the arrow macro.

The next thing we need to do is define a route. We talk about [routes](#routes)
in depth later on, but a route is basically a definition of an endpoint. 
Let's define a route and create a basic "Hello world" endpoint.

```clojure
(-> 
  (create-donkey)
  (create-server {:port   8080
                  :routes [{:handler (fn [_request respond _raise] 
                                       (respond {:body "Hello, world!"}))}]}))
  start
  (on-success (fn [_] (println "Server started listening on port 8080")))
``` 

As you can see we added a `:routes` key to the options map used to initialise 
the server. A route is a map that describes what kind of requests are 
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
  
In the handler we are calling the response callback `respond` with a response map
where the body of the response is "Hello, world!".

If you run the example and open a browser on `http://localhost:8080` you will
see a page with "Hello, World!".

### Routes

In Donkey HTTP requests are routed to handlers. When you initialise a server
you define a set of routes that it should handle. When a request arrives the 
server checks if one of the routes can handle the request. If no matching route 
is found, then a `404 Not Found` response is returned to the client.

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
based on the concept of a single threaded event loop that serves requests. An 
event loop is conceptually a long-running task with a queue of events it needs 
to dispatch. As long as events are dispatched "quickly" and don't occupy too 
much of the event loop's time, it can dispatch events at a very high rate. 
Because it is single threaded, or in other words serial, during the time it 
takes to dispatch one event no other event can be dispatched. Therefore, it's 
extremely important *not to block the event loop*.

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
1) Exact match. In the example above it means the route will only match requests 
to `http://localhost:8080/api/v2`. It will _not_ match requests to: 
    - `http://localhost:8080/api` 
    - `http://localhost:8080/api/v3` 
    - `http://localhost:8080/api/v2/user`
2) Path variables. Take for example the path `/api/v2/user/:id/address`. `:id`
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

### Support for Routing Libraries

Sometimes we have an existing service using some HTTP server and
routing libraries such as [Compojure](https://github.com/weavejester/compojure) 
or [reitit](https://github.com/metosin/reitit), and we don't have time to 
rewrite the routing logic right away. It's very easy to simply plug all your 
existing routing logic to Donkey without changing a line of code.

We'll use Compojure and reitit as examples, but the same goes for any other Ring
compatible library you use.

#### reitit

Here is an excerpt from Metosin's reitit 
[Ring-router](https://cljdoc.org/d/metosin/reitit/0.5.5/doc/introduction#ring-router)
documentation, demonstrating how to create a simple router.

```clojure
(require '[reitit.ring :as ring])

(defn handler [_]
  {:status 200, :body "ok"})

(defn wrap [handler id]
  (fn [request]
    (update (handler request) :wrap (fnil conj '()) id)))

(def app
  (ring/ring-handler
    (ring/router
      ["/api" {:middleware [[wrap :api]]}
       ["/ping" {:get handler
                 :name ::ping}]
       ["/admin" {:middleware [[wrap :admin]]}
        ["/users" {:get handler
                   :post handler}]]])))
```
Now let's see how you would use this router with Donkey.

```clojure
(-> 
  (create-donkey)
  (create-server {:port 8080 
                  :routes [{:handler app 
                            :handler-mode :blocking}]})
  start)
```  
That's it!

Basically, we're creating a single route that will match any request to the server 
and will delegate the routing logic and request handling to the reitit router. 
You'll notice we had to add `:handler-mode :blocking` to the route. That's 
because this particular example uses the one argument ring handler. If we add a 
three argument arity to `handler` and `wrap`, then we'll be able to remove 
`:handler-mode :blocking` and use the default non-blocking mode. 


#### Compojure

Here is an excerpt from James Reeves'
[Compojure](https://github.com/weavejester/compojure) repository on GitHub, 
demonstrating how to create a simple router.

```clojure
(ns hello-world.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))
```
To use this router with Donkey we do exactly the same thing we did for 
[reitit](#reitit)'s router.

```clojure
(-> 
  (create-donkey)
  (create-server {:port 8080 
                  :routes [{:handler app 
                            :handler-mode :blocking}]})
  start)
```   

### Middleware

#### Overview

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
 
#### Examples
 
Let's start with a one argument middleware that adds a timestamp to a request:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request] 
    (handler 
      (assoc request :timestamp (System/currentTimeMillis)))))
```

Now the same middleware with the non-blocking three arguments variant:
```clojure
(defn add-timestamp-middleware [handler]
  (fn [request respond raise]
    (try
      (handler
        (assoc request :timestamp (System/currentTimeMillis)) respond raise)
      (catch Exception ex
        (raise ex)))))
```

In the last examples we've been updating the request and calling
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

#### Common Middleware

There are some common operations that Donkey provides as pre-made middleware
that can be found under `com.appsflyer.donkey.middleware.*` namespaces.
All the middleware that come with Donkey take an optional options map. The
options map can be used, for example, to supply an exception handler.

A very common use case is inspecting the query parameters sent by a client in
the url of a GET request. By default, the query parameters are available in 
the request as a string under `:query-string`. It would be much more useful 
if we also had a map of name value pairs we can easily use.

```clojure
(:require [com.appsflyer.donkey.middleware.params :refer [parse-query-params]])

(->
  (create-donkey)
  (create-server {:port   8080
                  :routes [{:path       "/greet"
                            :methods    [:get]    
                            :handler    (fn [req res _err]
                                          (res {:body (str "Hello, "
                                                           (get-in req [:query-params "fname"])
                                                           " "
                                                           (get-in req [:query-params "lname"]))}))
                            :middleware [(parse-query-params)]}]})
  start)
```
 
In this example we are using the `parse-query-params` middleware, that does
exactly that. Now if we make a `GET` request 
`http://localhost:8080/greet?fname=foo&lname=bar` we'll get back: 
>Hello, foo bar

Another common use case is converting the names of each query parameter into 
a keyword. We can achieve both objectives with one middleware:

```clojure
(:require [com.appsflyer.donkey.middleware.params :refer [parse-query-params]])

(->
  (create-donkey)
  (create-server {:port   8080
                  :routes [{:path       "/greet"
                            :methods    [:get]    
                            :handler    (fn [req res _err]
                                          (res {:body (str "Hello, "
                                                           (-> req :query-params :fname)
                                                           " "
                                                           (-> req :query-params :lname))}))
                            :middleware [(parse-query-params {:keywordize true})]}]})
  start)
```

### Server Examples

Consumes & Produces (see [Routes](#routes) section) 
```clojure
(->
  (donkey/create-donkey)
  (donkey/create-server
    {:port   8080
     :routes [{:path         "/hello-world"
               :methods      [:get]
               :handler-mode :blocking
               :consumes     ["text/plain"]
               :produces     ["application/json"]
               :handler      (fn [request]
                               {:status 200
                                :body   "{\"greet\":\"Hello world!\"}"})}]})
  server/start)
```

Path variables (see [Routes](#routes) section) 
```clojure
(->
  (donkey/create-donkey)
  (donkey/create-server
    {:port   8080
     :routes [{:path     "/greet/:name"
               :methods  [:get]
               :consumes ["text/plain"]
               :handler  (fn [req respond _raise]
                           (respond
                             {:status  200
                              :headers {"content-type" "text/plain"}
                              :body    (str "Hello " (-> :path-params req (get "name")))}))}]})
  server/start)
```

## Client

The following examples assume these required namespaces
```clojure
(:require [com.appsflyer.donkey.core :as donkey]
          [com.appsflyer.donkey.client :refer [request stop]]
          [com.appsflyer.donkey.result :refer [on-complete on-success on-fail]]
          [com.appsflyer.donkey.request :refer [submit submit-form submit-multipart-form]])
```

### Creating a Client
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

### Stopping a Client

Once we're done with a client we should always stop it. This will release all 
the resources being held by the client, such as connections, event loops, etc'.
You should reuse a single client throughout the lifetime of the application,
and stop it only if it won't be used again. Once stopped it should not be used
again.
 
```clojure
(stop donkey-client)
```

### Creating a Request

When creating a request we supply an options map that defines it. The map has
to contain a `:method` key, and either an `:uri` or an `:url`. The `:uri` key
defines the location of the resource being requested, for example: 
```clojure
(-> 
  donkey-client
  (request {:method :get
            :uri    "/api/v1/users"}))
```

The `:url` key defines the absolute URL of the resource, for example: 
```clojure
(-> 
  donkey-client
  (request {:method :get
            :url    "http://www.example.com/api/v1/users"}))
```

When an `:url` is supplied then the `:uri`, `:port`, `:host` and `:ssl`
keys are ignored. 

### Submitting a Request

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
                   "upload-as"      "binary"}})
```  

### FutureResult

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


If the response is irrelevant as is the case in "call 
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

Although it is not recommended in the context of asynchronous operations, 
results can also be dereferenced:
```clojure
(let [result @(submit async-request)]
  (if (map? result)
    (println "Yea!")
    (println "Nay :(")))
``` 
In this case the call to `submit` will block the calling thread until a result
is available. The result may be either a response map, if the request was 
successful, or an `ExceptionInfo` if it wasn't.           

Each function returns a new `FutureResult` instance, which makes it possible 
to chain handlers. Let's look at an example:

```clojure
(ns com.appsflyer.donkey.exmaple
  (:require [com.appsflyer.donkey.result :as result])
  (:import (com.appsflyer.donkey FutureResult)))

; Chaning example. Each function gets the return value of the previous

(letfn [(increment [val]
                  (let [res (update val :count (fnil inc 0))]
                    (println res)
                    res))]
  (->
    (FutureResult/create {})
    (result/on-success increment)
    (result/on-success increment)
    (result/on-success increment)
    (result/on-fail (fn [_ex] (println "We have a problem"))))

; Output:
; {:count 1}
; {:count 2}
; {:count 3}
```

We start off by defining an `increment` function that takes a map and 
increments a `:counter` key. We then create a `FutureResult` that completes with
an empty map. The first example shows how chaining the result of one function 
to the next works. 

---

The rest of the examples assume the following vars are defined
 
```clojure
(def donkey-core (donkey/create-donkey))
(def donkey-client (donkey/create-client donkey-core)
```  
 
### HTTPS Requests

Making HTTPS requests requires setting `:ssl` to `true` and `:default-port` or 
`:port` when creating a client or a request respectively.

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
   
## Metrics

The library uses [Dropwizard](https://metrics.dropwizard.io/4.1.2/) to capture 
different metrics. The metrics can be largely grouped into three categories: 
- Thread Pool
- Server
- Client

Metrics collection can be set up when creating a `Donkey` by supplying a pre 
instantiated instance of `MetricRegistry`. It's the user's responsibility to implement
reporting to a monitoring backend such as [Prometheus](https://prometheus.io/), 
or [graphite](https://graphiteapp.org/). As later described, metrics are named 
using a dot `.` separator. By default, all metrics are prefixed with `donkey`, 
but it's also possible to supply a `:metrics-prefix` with the `:metric-registry`
to use a different string.    

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
Debug mode is activated when creating a server or a client with `:debug true`.
In this mode several loggers are set to log at the `trace` level. It means the
logs will be *very* verbose. For that reason it is not suitable for production
use, and should only be enabled in development as needed.

The logs include:
- All of Netty's low level networking, system configuration, memory leak 
detection logs and more. 
- Hexadecimal representation of each batch of packets being transmitted to the 
server. 
- Request routing, which is useful to debug a route that is not being matched.
- Donkey trace logs.   

  
### Logging
The library doesn't include any logging implementation, and can be used with any
[SLF4J](http://www.slf4j.org/) compatible logging library.
The exception is when running in `debug` mode. In order to dynamically change 
the logging level without forcing users to add XML configuration files, Donkey 
uses [Logback](http://logback.qos.ch/) as its implementation. It should be 
included on the project's classpath, otherwise a warning will be printed and
debug logging will be disabled.

## Troubleshooting

#### ClassNotFoundException - com.codahale.metrics.JmxAttributeGauge
```
Execution error (ClassNotFoundException) at jdk.internal.loader.BuiltinClassLoader/loadClass (BuiltinClassLoader.java:581). com.codahale.metrics.JmxAttributeGauge
```
Donkey has a transitive dependency `io.dropwizard.metrics/metrics-core` version 
4.X.X. If you are using a library that is dependent on version 3.X.X then you
could get a dependency collision. To avoid it you can exclude the dependency 
when importing Donkey. For example:

project.clj
```clojure
:dependencies [com.appsflyer/donkey "0.1.0" :exclusions [io.dropwizard.metrics/metrics-core]]
```   

deps.edn
```clojure
{:deps
 {com.appsflyer/donkey {:mvn/version "0.1.0"
                       :exclusions [io.dropwizard.metrics/metrics-core]}}}
```


## License

Copyright 2020 AppsFlyer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
