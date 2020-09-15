package com.appsflyer.donkey.server.router;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterFactory {
  
  static RouterFactory create(Vertx vertx, RouterDefinition routerDefinition) {
    return new RouterFactoryImpl(vertx, routerDefinition);
  }
  
  Router withRouteCreator(RouteCreatorFactory routeCreatorFactory);
}
