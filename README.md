# Donkey


## TODO
- Remove leak detector in production and add in debug mode
- Turn off / on vertx features in production / debug mode.
- Clean up the middleware code in route.clj
- Add more middleware tests
- TESTS!
- Client implementation
- Check about using OpenCensus rather than DropWizard for metrics
- Documentation
- Examples
- README
- Setup CI
- Explore having the metrics implementation pluggable by the user.
- Consider going completely data oriented. The config is just json, and the handlers 
are namespaced keywords point to user functions.

========================================

## DONE
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
to requests and responses.

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

### Debug mode
Debug mode is activated when creating the server with `:debug true`.
It will cause A LOT of logs to be written and therefore it is completely
unsuitable for production, and should only be used while debugging in development.
The logs include:
- Instantiation logs 
- Byte level traffic
- Request routing
- library debug logs   

  
   

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
