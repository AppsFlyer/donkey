# donkey


## TODO
- Path variables are not working - 404
- Metrics are not working
- Add support for middleware 
- Make number of event loops configurable
- TESTS!

========================================

## DONE




## Usage

Blocking handler mode. 
```clojure
(-> {:port   8080
       :routes [{:path     "/hello-world"
                 :methods  [:get]
                 :handler-mode :blocking
                 :consumes ["text/plain"]
                 :handler  (fn [req]
                             {:status  200
                              :headers {"content-type" "application/json"}
                              :body    (.getBytes "{\"greet\":\"Hello world!\"}")})}]}
      donkey/create-server
      server/start)
```

Non-blocking handler mode.
```clojure
(-> {:port   8080
       :routes [{:path            "/hello/:greet"
                 :methods         [:get]
                 :metrics-enabled true
                 :consumes        ["text/plain"]
                 :handler         (fn [req respond _raise]
                                    (future
                                      (respond
                                        {:status  200
                                         :headers {"content-type" "text/plain"}
                                         :body    (.getBytes
                                                    (str "Hello " (-> :path-params req (get "greet"))))})))}]}
      donkey/create-server
      server/start)
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
