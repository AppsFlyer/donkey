package com.appsflyer.donkey.route.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface HandlerFactory
{
  Handler<RoutingContext> requestHandler();
  
  Handler<RoutingContext> responseHandler(Vertx vertx);
}
