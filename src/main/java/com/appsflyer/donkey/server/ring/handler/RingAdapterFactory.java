package com.appsflyer.donkey.server.ring.handler;

import com.appsflyer.donkey.server.handler.*;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class RingAdapterFactory implements AdapterFactory {
  
  @Override
  public Handler<RoutingContext> requestAdapter() {
    return new RingRequestAdapter();
  }
  
  @Override
  public Handler<RoutingContext> responseAdapter() {
    return new RingResponseAdapter();
  }
  
}
