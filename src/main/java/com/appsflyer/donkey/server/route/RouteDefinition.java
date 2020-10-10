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
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;

/**
 * The class is used to describe how to construct a {@link io.vertx.ext.web.Route}.
 */
public interface RouteDefinition {
  
  static RouteDefinition create() {
    return new RouteDefinitionImpl();
  }
  
  PathDefinition path();
  
  RouteDefinition path(PathDefinition path);
  
  RouteDefinition path(String path);
  
  Collection<HttpMethod> methods();
  
  RouteDefinition addMethod(HttpMethod method);
  
  Collection<String> consumes();
  
  RouteDefinition addConsumes(String contentType);
  
  Collection<String> produces();
  
  RouteDefinition addProduces(String contentType);
  
  Handler<RoutingContext> handler();
  
  RouteDefinition handler(Handler<RoutingContext> handler);
  
  HandlerMode handlerMode();
  
  RouteDefinition handlerMode(HandlerMode handlerMode);
}
