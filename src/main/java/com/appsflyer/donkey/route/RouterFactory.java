package com.appsflyer.donkey.route;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterFactory {
  
  static RouterFactory create(Vertx vertx, RouterDefinition routerDefinition) {
    return new RouterFactoryImpl(vertx, routerDefinition);
  }
  
  Router withRouteCreator(RouteCreatorSupplier routeCreatorSupplier);
}
