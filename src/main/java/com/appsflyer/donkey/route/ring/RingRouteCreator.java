package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.AbstractRouteCreator;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.AdapterFactory;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import com.appsflyer.donkey.route.handler.ring.RingAdapterFactory;
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
    addBodyHandler(route);
    addRequestAdapter(route);
    addHandler(route, rd.handler(), rd.handlerMode());
    addResponseAdapter(route);
  }
  
  private void addRequestAdapter(Route route) {
    route.handler(adapterFactory.requestAdapter());
  }
  
  private void addResponseAdapter(Route route) {
    route.handler(adapterFactory.responseAdapter());
  }
}
