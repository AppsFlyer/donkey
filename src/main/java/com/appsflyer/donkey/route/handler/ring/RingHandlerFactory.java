package com.appsflyer.donkey.route.handler.ring;

import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class RingHandlerFactory implements HandlerFactory
{
  @Override
  public Handler<RoutingContext> requestHandler()
  {
    return new RingRequestHandler();
  }
  
  @Override
  public Handler<RoutingContext> responseHandler(Vertx vertx)
  {
    Objects.requireNonNull(vertx, "Cannot create response handler without Vertx");
    return new RingResponseHandler(vertx);
  }
  
  @Override
  public Handler<RoutingContext> handlerFor(RouteDescriptor route)
  {
    Objects.requireNonNull(route, "Cannot create route handler without RouteDescriptor");
    
    return switch (route.handlerMode()) {
      case BLOCKING -> new BlockingRingHandler(route.handler());
      case NON_BLOCKING -> new RingHandler(route.handler());
    };
  }
}
