package com.appsflyer.donkey.client.ring;

import com.appsflyer.donkey.server.ring.handler.RingHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static com.appsflyer.donkey.ClojureObjectMapper.serialize;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

public class VertxRouteSupplier {
  
  private static final ObjectMapper mapper = new ObjectMapper();
  
  private static void returnRequest(RoutingContext ctx) {
    ctx.response()
       .end(Buffer.buffer(
           serialize(ctx.get(RingHandler.RING_HANDLER_RESULT))));
    
  }
  
  public Route root200(Router router) {
    return router.route()
                 .path("/")
                 .handler(ctx -> ctx.response().end());
  }
  
  public Route echo(Router router) {
    return router.route()
                 .path("/echo")
                 .handler((RingHandler) VertxRouteSupplier::returnRequest);
  }
  
  public Route postFormOrFile(Router router) {
    return router.route()
                 .path("/post/form")
                 .method(POST)
                 .consumes(APPLICATION_X_WWW_FORM_URLENCODED.toString())
                 .consumes(MULTIPART_FORM_DATA.toString())
                 .consumes(APPLICATION_OCTET_STREAM.toString())
                 .handler(VertxRouteSupplier::returnRequest);
  }
  
  
  public Route getPathVariable(Router router, String uri) {
    return router.route()
                 .method(GET)
                 .path(uri)
                 .handler(ctx -> {
                   try {
                     ctx.response()
                        .end(Buffer.buffer(
                            mapper.writeValueAsBytes(ctx.pathParams())));
                   } catch (JsonProcessingException e) {
                     throw new RuntimeException(e);
                   }
                 });
  }
  
  public Route getRegexPath(Router router, String uri) {
    return router.route()
                 .method(GET)
                 .pathRegex(uri)
                 .handler(ctx -> {
                   try {
                     ctx.response()
                        .end(Buffer.buffer(
                            mapper.writeValueAsBytes(ctx.pathParams())));
                   } catch (JsonProcessingException e) {
                     throw new RuntimeException(e);
                   }
                 });
  }
  
}
