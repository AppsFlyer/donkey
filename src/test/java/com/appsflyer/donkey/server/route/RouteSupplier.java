package com.appsflyer.donkey.server.route;

import io.vertx.junit5.Checkpoint;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpMethod.*;

public interface RouteSupplier {
  
  default RouteDefinition helloWorld(Checkpoint requestsServed) {
    return RouteDefinition
        .create()
        .path("/")
        .addMethod(GET)
        .handler(ctx -> {
          ctx.response().end("Hello, World!");
          requestsServed.flag();
        });
  }
  
  default RouteDefinition postOrPutJson(Checkpoint requestsServed, String jsonResponse) {
    return RouteDefinition
        .create()
        .path("/json")
        .addMethod(POST)
        .addMethod(PUT)
        .addConsumes(APPLICATION_JSON.toString())
        .addProduces(APPLICATION_JSON.toString())
        .handler(ctx -> {
          ctx.response().end(jsonResponse);
          requestsServed.flag();
        });
  }
  
  RouteDefinition echo(Checkpoint requestsServed);
  
  RouteDefinition postFormOrFile(Checkpoint requestsServed);
  
  RouteDefinition getPathVariable(Checkpoint requestsServed, String uri);
  
  RouteDefinition getRegexPath(Checkpoint requestsServed, String uri);
}
