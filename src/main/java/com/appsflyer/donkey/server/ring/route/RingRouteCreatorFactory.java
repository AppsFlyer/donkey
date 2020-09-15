package com.appsflyer.donkey.server.ring.route;

import com.appsflyer.donkey.server.route.AbstractRouteCreator;
import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.router.RouterDefinition;
import io.vertx.ext.web.Router;

public class RingRouteCreatorFactory implements RouteCreatorFactory {
  
  @Override
  public AbstractRouteCreator newInstance(Router router, RouterDefinition routerDefinition) {
    return new RingRouteCreator(router, routerDefinition);
  }
}
