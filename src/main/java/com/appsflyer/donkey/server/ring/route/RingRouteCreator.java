package com.appsflyer.donkey.server.ring.route;

import com.appsflyer.donkey.server.route.AbstractRouteCreator;
import com.appsflyer.donkey.server.route.RouteDescriptor;
import com.appsflyer.donkey.server.handler.AdapterFactory;
import com.appsflyer.donkey.server.router.RouterDefinition;
import com.appsflyer.donkey.server.ring.handler.RingAdapterFactory;
import com.appsflyer.donkey.server.ring.handler.RingHandler;
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
