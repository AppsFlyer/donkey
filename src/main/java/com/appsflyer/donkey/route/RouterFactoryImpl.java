package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.DateHeaderGenerator;
import com.appsflyer.donkey.route.handler.error.NotFoundErrorHandler;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import com.appsflyer.donkey.route.handler.error.InternalServerErrorHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

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
          .errorHandler(404, new NotFoundErrorHandler())
          .route()
          .handler(DateHeaderGenerator.getInstance(vertx))
          .handler(ResponseContentTypeHandler.create());
  }
  
  @Override
  public Router withRouteCreator(RouteCreatorSupplier routeCreatorSupplier) {
    return routeCreatorSupplier.supply(router, routerDefinition).addRoutes();
  }
}
