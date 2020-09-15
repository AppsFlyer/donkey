package com.appsflyer.donkey.server.ring.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@FunctionalInterface
public interface RingHandler extends Handler<RoutingContext> {
  
  String LAST_HANDLER_RESPONSE_FIELD = "handler-response";
}
