package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.HandlerFactory;
import com.appsflyer.donkey.route.handler.InternalServerErrorHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.util.*;

import static io.vertx.core.http.HttpMethod.*;

public class RouterFactory
{
  private final Router router;
  private final Vertx vertx;
  private final HandlerFactory handlerFactory;
  private static final Collection<HttpMethod> METHODS_WITH_BODY = EnumSet.of(POST, PUT, PATCH);
  
  public RouterFactory(Vertx vertx, HandlerFactory handlerFactory)
  {
    this.vertx = vertx;
    this.handlerFactory = handlerFactory;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler(vertx));
    addContentTypeHandler();
  }
  
  private void addContentTypeHandler()
  {
    router.route().handler(ResponseContentTypeHandler.create());
  }
  
  private boolean hasBody(Route route)
  {
    var methods = route.methods();
    if (methods == null) {
      return true;
    }
    return route.methods().stream().anyMatch(METHODS_WITH_BODY::contains);
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
  
  //This overloaded version is package-private and only used in tests for convenience.
  @SuppressWarnings("OverloadedVarargsMethod")
  Router withRoutes(RouteDescriptor... routes)
  {
    Objects.requireNonNull(routes, "Cannot create router with null routes");
    return withRoutes(List.of(routes));
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
    if (hasBody(route)) {
      route.handler(BodyHandler.create());
    }
    
    route.handler(handlerFactory.requestHandler());
    
    rd.handlers().forEach(handler -> {
      if (rd.handlerMode() == HandlerMode.BLOCKING) {
        route.blockingHandler(handler);
      }
      else {
        route.handler(handler);
      }
    });
    
    route.handler(handlerFactory.responseHandler(vertx));
  }
}
