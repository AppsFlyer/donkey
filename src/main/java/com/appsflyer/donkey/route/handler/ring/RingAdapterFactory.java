package com.appsflyer.donkey.route.handler.ring;

import com.appsflyer.donkey.route.handler.AdapterFactory;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class RingAdapterFactory implements AdapterFactory
{
  @Override
  public Handler<RoutingContext> requestAdapter()
  {
    return new RingRequestAdapter();
  }
  
  @Override
  public Handler<RoutingContext> responseAdapter()
  {
    return new RingResponseAdapter();
  }
  
}
