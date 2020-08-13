package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.RouteDescriptor;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HandlerFactoryStub implements HandlerFactory
{
  @Override
  public Handler<RoutingContext> requestHandler()
  {
    return RoutingContext::next;
  }
  
  @Override
  public Handler<RoutingContext> responseHandler(Vertx vertx)
  {
    return BodyHandler.create();
  }
  
  @Override
  public Handler<RoutingContext> handlerFor(RouteDescriptor route)
  {
    return ctx -> route.handler().apply(ctx);
  }
}
