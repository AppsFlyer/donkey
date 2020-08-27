package com.appsflyer.donkey.route.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class ServerHeaderHandler implements Handler<RoutingContext> {
  
  private static final String SERVER_PROP = "Server";
  static final String SERVER_NAME = "Donkey";
  
  public static ServerHeaderHandler create() {
    return new ServerHeaderHandler();
  }
  
  @Override
  public void handle(RoutingContext ctx) {
    ctx.addHeadersEndHandler(v -> ctx.response().putHeader(SERVER_PROP, SERVER_NAME));
    ctx.next();
  }
  
}
