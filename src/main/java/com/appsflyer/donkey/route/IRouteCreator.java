package com.appsflyer.donkey.route;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public interface IRouteCreator {
  Router addRoutes();
  
  void setPath(Route route, RouteDescriptor rd);
  
  void addBodyHandler(Route route);
  
  void addMiddleware(Route route, RouteDescriptor rd);
  
  void addHandler(Route route, Handler<RoutingContext> handler, HandlerMode handlerMode);
}
