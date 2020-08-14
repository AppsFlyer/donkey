package com.appsflyer.donkey.route.handler.ring;

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
  
}
