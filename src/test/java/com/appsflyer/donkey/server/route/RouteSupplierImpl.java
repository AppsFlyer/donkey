package com.appsflyer.donkey.server.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Checkpoint;

import static com.appsflyer.donkey.server.route.PathDefinition.MatchType.REGEX;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

public class RouteSupplierImpl implements RouteSupplier {
  
  private static final ObjectMapper mapper = new ObjectMapper();
  
  private static Handler<RoutingContext> returnRequest(Checkpoint requestsServed) {
    return ctx -> {
      try {
        ctx.response()
           .end(Buffer.buffer(
               mapper.writeValueAsBytes(ctx.request())));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      requestsServed.flag();
    };
  }
  
  @Override
  public RouteDefinition echo(Checkpoint requestsServed) {
    return RouteDefinition.create()
                           .path("/echo")
                           .handler(returnRequest(requestsServed));
  }
  
  @Override
  public RouteDefinition postFormOrFile(Checkpoint requestsServed) {
    return RouteDefinition.create()
                           .path("/post/form")
                           .addMethod(POST)
                           .addConsumes(APPLICATION_X_WWW_FORM_URLENCODED.toString())
                           .addConsumes(MULTIPART_FORM_DATA.toString())
                           .addConsumes(APPLICATION_OCTET_STREAM.toString())
                           .handler(returnRequest(requestsServed));
  }
  
  @Override
  public RouteDefinition getPathVariable(Checkpoint requestsServed, String uri) {
    return RouteDefinition
        .create()
        .addMethod(GET)
        .path(uri)
        .handler(ctx -> {
          try {
            ctx.response()
               .end(Buffer.buffer(
                   mapper.writeValueAsBytes(ctx.pathParams())));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          requestsServed.flag();
        });
  }
  
  @Override
  public RouteDefinition getRegexPath(Checkpoint requestsServed, String uri) {
    return RouteDefinition
        .create()
        .addMethod(GET)
        .path(PathDefinition.create(uri, REGEX))
        .handler(ctx -> {
          try {
            ctx.response()
               .end(Buffer.buffer(
                   mapper.writeValueAsBytes(ctx.pathParams())));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          requestsServed.flag();
        });
  }
}
