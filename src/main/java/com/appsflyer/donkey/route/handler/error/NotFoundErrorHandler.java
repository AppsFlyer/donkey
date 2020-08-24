package com.appsflyer.donkey.route.handler.error;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotFoundErrorHandler implements Handler<RoutingContext> {
  private static final Logger logger = LoggerFactory.getLogger(NotFoundErrorHandler.class.getName());
  @Override
  public void handle(RoutingContext ctx) {
    logger.debug("Resource not found {}", ctx.normalisedPath());
    ctx.response().setStatusCode(404).end();
  }
}
