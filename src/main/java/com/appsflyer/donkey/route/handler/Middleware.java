package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.HandlerMode;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class Middleware
{
  private final Handler<RoutingContext> handler;
  private final HandlerMode handlerMode;
  
  public Middleware(Handler<RoutingContext> handler, HandlerMode handlerMode)
  {
    this.handler = handler;
    this.handlerMode = handlerMode;
  }
  
  public Middleware(Handler<RoutingContext> handler)
  {
    this(handler, HandlerMode.NON_BLOCKING);
  }
  
  public Handler<RoutingContext> handler()
  {
    return handler;
  }
  
  public HandlerMode handlerMode()
  {
    return handlerMode;
  }
}
