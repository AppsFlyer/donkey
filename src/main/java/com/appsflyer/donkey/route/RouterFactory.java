package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.HandlerConfig;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import com.appsflyer.donkey.route.handler.InternalServerErrorHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import static io.vertx.core.http.HttpMethod.*;

public class RouterFactory
{
  private final Router router;
  private final Vertx vertx;
  private final HandlerConfig handlerConfig;
  private static final Collection<HttpMethod> METHODS_WITH_BODY = EnumSet.of(POST, PUT, PATCH);
  
  public RouterFactory(Vertx vertx, HandlerConfig handlerConfig)
  {
    Objects.requireNonNull(vertx, "Vertx argument is missing");
    Objects.requireNonNull(handlerConfig, "Handler config argument is missing");
    
    this.vertx = vertx;
    this.handlerConfig = handlerConfig;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler(vertx));
  }
  
  private boolean hasBody(Route route)
  {
    var methods = route.methods();
    if (methods == null) {
      return true;
    }
    return route.methods().stream().anyMatch(METHODS_WITH_BODY::contains);
  }
  
  public Router create()
  {
    router.route().handler(ResponseContentTypeHandler.create());
    handlerConfig.routes()
                 .stream()
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
    if (hasBody(route)) {
      route.handler(BodyHandler.create());
    }
    HandlerFactory handlerFactory = handlerConfig.handlerFactory();
    route.handler(handlerFactory.requestHandler());
    
    addMiddleware(route);
    
    rd.handlers().forEach(handler -> addHandler(route, handler, rd.handlerMode()));
    
    route.handler(handlerFactory.responseHandler(vertx));
  }
  
  private void addMiddleware(Route route)
  {
    route.handler(ResponseContentTypeHandler.create());
    handlerConfig.middleware().forEach(
        middleware -> addHandler(route, middleware.handler(), middleware.handlerMode()));
  }
  
  private void addHandler(Route route, Handler<RoutingContext> handler, HandlerMode handlerMode)
  {
    if (handlerMode == HandlerMode.BLOCKING) {
      route.blockingHandler(handler);
    }
    else {
      route.handler(handler);
    }
  }
}
