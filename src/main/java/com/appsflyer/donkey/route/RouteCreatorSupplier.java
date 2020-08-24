package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.RouterDefinition;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouteCreatorSupplier {
  
  RouteCreator supply(Router router, RouterDefinition routerDefinition);
}
