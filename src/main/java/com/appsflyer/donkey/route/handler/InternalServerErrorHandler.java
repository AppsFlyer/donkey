package com.appsflyer.donkey.route.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class InternalServerErrorHandler extends ResponseBuilder implements Handler<RoutingContext>
{
  
  public InternalServerErrorHandler(Vertx vertx)
  {
    super(vertx);
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    addDefaultHeaders(ctx.response());
    ctx.response().setStatusCode(500).end();
  }
}
