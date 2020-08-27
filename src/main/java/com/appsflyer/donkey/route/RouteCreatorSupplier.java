package com.appsflyer.donkey.route;

import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouteCreatorSupplier {
  
  RouteCreator supply(Router router, RouterDefinition routerDefinition);
}
