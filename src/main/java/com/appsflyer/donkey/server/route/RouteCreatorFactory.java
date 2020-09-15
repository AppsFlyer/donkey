package com.appsflyer.donkey.server.route;

import com.appsflyer.donkey.server.router.RouterDefinition;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouteCreatorFactory {
  
  RouteCreator newInstance(Router router, RouterDefinition routerDefinition);
}
