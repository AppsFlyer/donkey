package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.AbstractRouteCreator;
import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.ext.web.Router;

public class RingRouteCreatorSupplier implements RouteCreatorSupplier {
  
  @Override
  public AbstractRouteCreator supply(Router router, RouterDefinition routerDefinition) {
    return new RingRouteCreator(router, routerDefinition);
  }
}
