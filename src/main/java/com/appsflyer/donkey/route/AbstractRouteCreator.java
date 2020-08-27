package com.appsflyer.donkey.route;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Collection;
import java.util.EnumSet;

import static com.appsflyer.donkey.route.PathDescriptor.MatchType.REGEX;
import static io.vertx.core.http.HttpMethod.*;

public abstract class AbstractRouteCreator implements RouteCreator {
  private static final Collection<HttpMethod> METHODS_WITH_BODY = EnumSet.of(POST, PUT, PATCH);
  private final Router router;
  private final Collection<RouteDescriptor> routeDescriptors;
  
  protected AbstractRouteCreator(Router router, RouterDefinition routerDefinition) {
    this.router = router;
    routeDescriptors = routerDefinition.routes();
  }
  
  @Override
  public Router addRoutes() {
    routeDescriptors.forEach(rd -> buildRoute(router.route(), rd));
    return router;
  }
  
  protected abstract void buildRoute(Route route, RouteDescriptor rd);
  
  private boolean hasBody(Route route) {
    var methods = route.methods();
    if (methods == null) {
      return true;
    }
    return route.methods().stream().anyMatch(METHODS_WITH_BODY::contains);
  }
  
  @Override
  public void setPath(Route route, RouteDescriptor rd) {
    if (rd.path() != null) {
      if (rd.path().matchType() == REGEX) {
        route.pathRegex(rd.path().value());
      } else {
        route.path(rd.path().value());
      }
    }
    rd.methods().forEach(route::method);
    rd.consumes().forEach(route::consumes);
    rd.produces().forEach(route::produces);
  }
  
  @Override
  public void addBodyHandler(Route route) {
    if (hasBody(route)) {
      route.handler(BodyHandler.create());
    }
  }
  
  @Override
  public void addHandler(Route route, Handler<RoutingContext> handler, HandlerMode handlerMode) {
    if (handlerMode == HandlerMode.BLOCKING) {
      route.blockingHandler(handler);
    } else {
      route.handler(handler);
    }
  }
}
