package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.RouteCreator;
import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import io.vertx.ext.web.Router;

public class RingRouteCreatorSupplier implements RouteCreatorSupplier {
  
  @Override
  public RouteCreator supply(Router router, RouterDefinition routerDefinition) {
    return new RingRouteCreator(router, routerDefinition);
  }
}
