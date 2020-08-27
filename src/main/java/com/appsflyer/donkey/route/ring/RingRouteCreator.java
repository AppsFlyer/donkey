package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.AbstractRouteCreator;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.AdapterFactory;
import com.appsflyer.donkey.route.RouterDefinition;
import com.appsflyer.donkey.route.handler.ring.RingAdapterFactory;
import com.appsflyer.donkey.route.handler.ring.RingHandler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class RingRouteCreator extends AbstractRouteCreator {
  
  private final AdapterFactory adapterFactory;
  
  RingRouteCreator(Router router, RouterDefinition routerDefinition) {
    super(router, routerDefinition);
    adapterFactory = new RingAdapterFactory();
  }
  
  @Override
  protected void buildRoute(Route route, RouteDescriptor rd) {
    setPath(route, rd);
    if (rd.handler() instanceof RingHandler) {
      addBodyHandler(route);
      addRequestAdapter(route);
      addHandler(route, rd.handler(), rd.handlerMode());
      addResponseAdapter(route);
    } else {
      addHandler(route, rd.handler(), rd.handlerMode());
    }
  }
  
  private void addRequestAdapter(Route route) {
    route.handler(adapterFactory.requestAdapter());
  }
  
  private void addResponseAdapter(Route route) {
    route.handler(adapterFactory.responseAdapter());
  }
}
