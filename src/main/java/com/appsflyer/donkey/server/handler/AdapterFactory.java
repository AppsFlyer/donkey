package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface AdapterFactory {
  
  Handler<RoutingContext> requestAdapter();
  
  Handler<RoutingContext> responseAdapter();
}
