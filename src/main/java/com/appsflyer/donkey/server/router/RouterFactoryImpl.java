package com.appsflyer.donkey.server.router;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.handler.NotFoundErrorHandler;
import com.appsflyer.donkey.server.handler.InternalServerErrorHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import java.util.Objects;

public class RouterFactoryImpl implements RouterFactory {
  
  private final Router router;
  private final RouterDefinition routerDefinition;
  
  RouterFactoryImpl(Vertx vertx, RouterDefinition routerDefinition) {
    Objects.requireNonNull(vertx, "Vertx argument is missing");
    Objects.requireNonNull(routerDefinition, "Router definition argument is missing");
    
    this.routerDefinition = routerDefinition;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler())
          .errorHandler(404, new NotFoundErrorHandler());
  }
  
  @Override
  public Router withRouteCreator(RouteCreatorFactory routeCreatorFactory) {
    return routeCreatorFactory.newInstance(router, routerDefinition).addRoutes();
  }
}
