package com.appsflyer.donkey.route.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalServerErrorHandler extends ResponseHandler
{
  private static final Logger logger = LoggerFactory.getLogger(InternalServerErrorHandler.class.getName());
  public InternalServerErrorHandler(Vertx vertx)
  {
    super(vertx);
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    Throwable ex = ctx.failure();
    logger.error("Unhandled exception:", ex);
    addDefaultHeaders(ctx.response());
    ctx.response().setStatusCode(500).end();
  }
}
