package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.InternalServerErrorHandler;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RouterFactory
{
  private final Router router;
  private final Vertx vertx;
  private final HandlerFactory handlerFactory;
  
  public RouterFactory(Vertx vertx, HandlerFactory handlerFactory)
  {
    this.vertx = vertx;
    this.handlerFactory = handlerFactory;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler(vertx));
    addBodyAggregator();
    addRequestBuilder();
  }
  
  private void addBodyAggregator()
  {
    router.route()
          .method(HttpMethod.POST)
          .method(HttpMethod.PUT)
          .method(HttpMethod.PATCH)
          .handler(BodyHandler.create());
  }
  
  private void addRequestBuilder()
  {
    router.route().handler(handlerFactory.requestHandler());
  }
  
  
  public Router withRoutes(List<RouteDescriptor> routes)
  {
    Objects.requireNonNull(routes, "Cannot create router with null routes");
    routes.stream()
          .map(rd -> Map.entry(rd, router.route()))
          .peek(this::buildRoute)
          .forEach(this::addHandlers);
    return router;
  }
  
  private void buildRoute(Map.Entry<RouteDescriptor, Route> entry)
  {
    RouteDescriptor rd = entry.getKey();
    Route route = entry.getValue();
    
    if (rd.path() != null) {
      if (rd.path().matchType() == PathDescriptor.MatchType.REGEX) {
        route.pathRegex(rd.path().value());
      }
      else {
        route.path(rd.path().value());
      }
    }
    rd.methods().forEach(route::method);
    rd.consumes().forEach(route::consumes);
    rd.produces().forEach(route::produces);
  }
  
  private void addHandlers(Map.Entry<RouteDescriptor, Route> entry)
  {
    RouteDescriptor rd = entry.getKey();
    Route route = entry.getValue();
    
    Handler<RoutingContext> handler = handlerFactory.handlerFor(rd);
    if (rd.handlerMode() == HandlerMode.BLOCKING) {
      route.blockingHandler(handler);
    }
    else {
      route.handler(handler);
    }
    route.handler(handlerFactory.responseHandler(vertx));
  }
}
