package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterFactory {
  
  static RouterFactory create(Vertx vertx, RouterDefinition routerDefinition) {
    return new RouterFactoryImpl(vertx, routerDefinition);
  }
  
  Router withRouteCreator(RouteCreatorSupplier routeCreatorSupplier);
}
