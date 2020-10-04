/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Collection;
import java.util.EnumSet;

import static com.appsflyer.donkey.server.route.PathDefinition.MatchType.REGEX;
import static io.vertx.core.http.HttpMethod.*;

public abstract class AbstractRouteCreator implements RouteCreator {
  private static final Collection<HttpMethod> METHODS_WITH_BODY = EnumSet.of(POST, PUT, PATCH);
  private final Router router;
  private final Collection<RouteDefinition> routeDefinitions;
  
  protected AbstractRouteCreator(Router router, RouteList routeList) {
    this.router = router;
    routeDefinitions = routeList.routes();
  }
  
  @Override
  public Router addRoutes() {
    routeDefinitions.forEach(rd -> buildRoute(router.route(), rd));
    return router;
  }
  
  protected abstract void buildRoute(Route route, RouteDefinition rd);
  
  private boolean hasBody(Route route) {
    var methods = route.methods();
    if (methods == null) {
      return true;
    }
    return route.methods().stream().anyMatch(METHODS_WITH_BODY::contains);
  }
  
  @Override
  public void setPath(Route route, RouteDefinition rd) {
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
