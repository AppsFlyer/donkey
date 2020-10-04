
## TODO
- Setup coveralls when repo is public
- Setup release stage in CI to clojars and maven central  
- Add routing benchmarks to compare with reitit 
- Look at response validation (expectation)
https://vertx.io/docs/vertx-web-client/java/#response-predicates
- Look into functional builder pattern rather than using a configuration map.
Examples:
https://dave.cheney.net/2014/10/17/functional-options-for-friendly-apis
https://github.com/AppsFlyer/unleash-client-clojure/blob/master/test/unleash_client_clojure/builder_test.clj
- Go over donkey-spec with Ben, work on dependant types.  
- See about adding health check support.
It's probably going to be simple enough to create an "alive" health check
without user interference (or with minimal such as port and uri).
A "healthy" health check would require the user to provide a handler. 
- Add more middleware tests
- TESTS!
- Documentation
- Examples
- README

========================================

## DONE
- Check if 5 seconds timeout for server start up is a good default. Maybe it should be increased?
- Clean up the middleware code in route.clj
- Check if there's a need to implement the `wrap-remove-amps` from af-nonblocking
to replace encoded ampersands.
Deferring it for now - if people need this they can use af-nonblocking-ring-middleware.
- Look into implementing JSON serialization / deserialization middleware
- Move project to AppsFlyer github repo.
- Setup CI
- Check if there's a need to implement the `wrap-prevent-header-injection` from af-nonblocking
to remove carriage return and line feed characters. ** Already implemented in Vert.x
https://github.com/eclipse-vertx/vert.x/commit/1bb6445226c39a95e7d07ce3caaf56828e8aab72
- Consider removing the JMX support for metrics - make it the user problem.
- First "create server" example without a route throws an exception.
- Lowercase all header names
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
