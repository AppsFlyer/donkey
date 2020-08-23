package com.appsflyer.donkey.route.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class AdapterFactoryStub implements AdapterFactory {
  @Override
  public Handler<RoutingContext> requestAdapter() {
    return RoutingContext::next;
  }
  
  @Override
  public Handler<RoutingContext> responseAdapter() {
    return BodyHandler.create();
  }
  
}
