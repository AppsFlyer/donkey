package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public interface RouteCreator {
  
  Router addRoutes();
  
  void setPath(Route route, RouteDescriptor rd);
  
  void addBodyHandler(Route route);
  
  void addHandler(Route route, Handler<RoutingContext> handler, HandlerMode handlerMode);
}
