# donkey


## TODO
- Clean up the middleware code in route.clj
- Add more middleware tests
- Make adding the "Date" header optional
- TESTS!
- Implement debug mode
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

### Ring Middleware

When using a routing library (@todo link to section about routing) that follows
the [Ring middleware](https://github.com/ring-clojure/ring/wiki/Concepts#middleware) concept,
you supply a [higher-order function](https://clojure.org/guides/higher_order_functions)
that accepts a `handler`, and zero or more optional arguments. The function should 
return a function that accepts 1 or 3 arguments, that is responsible calling `handler`.

For example:
```clojure
(defn add-timestamp-middleware [handler]
    (fn [request] (handler (assoc request :timestamp (System/currentTimeMillis)))))
```
   
### Donkey Handlers

The routing API doesn't have the same separation between middleware and handlers. 
In fact middleware are just handlers. Each handler is called in order with the 
result emitted by the previous handler.

For example:
```clojure 
(defn add-timestamp-middleware [request respond raise]
    (respond (assoc request :timestamp (System/currentTimeMillis))))
``` 

Or when `:handler-mode` is `:blocking` then the next handler is called with the 
value returned by the previous handler.
```clojure
(defn add-timestamp-middleware [request]
    (assoc request :timestamp (System/currentTimeMillis)))
```
 
Notice that we ran the first handler on the calling thread. That's because there
would be no benefit in off loading a simple `assoc` to a separate thread.
But, if for example we have a handler that communicates with a remote database
then we should run it on a separate thread.
```clojure
(defn add-user-id [request respond raise]
    (future 
      (respond 
        (assoc request :user-id (get-id-from-db 
                                     (-> request :query-params (get "user-email")))))))
```

Here's an example of a route that uses handlers to modify the request and response.
```clojure
{:path     "/timestamp"
 :methods  [:get]
 :handlers [(fn [request respond raise]
              (respond (assoc request :timestamp (System/currentTimeMillis))))
            (fn [request respond raise]
              (respond {:status (if (even? (:timestamp request)) 200 400)}))
           (fn [response respond raise]
              (respond (assoc response :body (if (= 200 (:status response)) "Timestamp is even!" "Timestamp id odd :("))))]}
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
