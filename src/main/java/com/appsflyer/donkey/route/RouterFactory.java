package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.RouterDefinition;
import com.appsflyer.donkey.route.handler.InternalServerErrorHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.util.Objects;

public class RouterFactory {
  private final Router router;
  private final RouterDefinition routerDefinition;
  
  public RouterFactory(Vertx vertx, RouterDefinition routerDefinition) {
    Objects.requireNonNull(vertx, "Vertx argument is missing");
    Objects.requireNonNull(routerDefinition, "Handler config argument is missing");
    
    this.routerDefinition = routerDefinition;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler())
          .route()
          .handler(ResponseContentTypeHandler.create());
  }
  
  public Router create(RouteCreatorSupplier routeCreatorSupplier) {
    return routeCreatorSupplier.supply(router, routerDefinition).addRoutes();
  }
}
