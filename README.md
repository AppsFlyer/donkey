# donkey


## TODO
- TESTS!
- Implement debug mode
- Client implementation
- Check about using OpenCensus rather than DropWizard for metrics
- Documentation
- Examples
- README
- Setup CI

========================================

## DONE
- Add support for middleware 
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
               :handlers     [(fn [_req]
                                {:status  200
                                 :headers {"content-type" "application/json"}
                                 :body    (.getBytes "{\"greet\":\"Hello world!\"}")})]}]}
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
               :handlers        [(fn [req respond _raise]
                                   (future
                                     (respond
                                       {:status  200
                                        :headers {"content-type" "text/plain"}
                                        :body    (.getBytes
                                                   (str "Hello " (-> :path-params req (get "name"))))})))]}]}
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
