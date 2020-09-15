package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalServerErrorHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(InternalServerErrorHandler.class.getName());
  
  @Override
  public void handle(RoutingContext ctx)
  {
    logger.error("Unhandled exception:", ctx.failure());
    ctx.response().setStatusCode(500);
  }
}
