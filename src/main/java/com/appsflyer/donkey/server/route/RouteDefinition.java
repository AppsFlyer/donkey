/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

public final class RouteDefinition {
  
  public static RouteDefinition create() {
    return new RouteDefinition();
  }
  
  private static void assertNonEmptyContentType(String val) {
    Objects.requireNonNull(val, "contentType cannot be null");
    if (val.isBlank()) {
      throw new IllegalArgumentException(String.format("Invalid content type: %s", val));
    }
  }
  
  private final Collection<HttpMethod> methods = new HashSet<>();
  private final Collection<String> consumes = new HashSet<>(6);
  private final Collection<String> produces = new HashSet<>(6);
  private HandlerMode handlerMode = HandlerMode.NON_BLOCKING;
  private Handler<RoutingContext> handler;
  private PathDefinition path;
  
  private RouteDefinition() {
  }
  
  public PathDefinition path() {
    return path;
  }
  
  public RouteDefinition path(String path) {
    return path(PathDefinition.create(path));
  }
  
  public RouteDefinition path(PathDefinition path) {
    this.path = path;
    return this;
  }
  
  public Collection<HttpMethod> methods() {
    if (methods.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(methods);
  }
  
  /**
   * Can be called multiple times to add multiple HTTP verbs
   */
  public RouteDefinition addMethod(HttpMethod method) {
    Objects.requireNonNull(method, "method cannot be null");
    methods.add(method);
    return this;
  }
  
  public Collection<String> consumes() {
    if (consumes.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(consumes);
  }
  
  /**
   * Can be called multiple times to add multiple content types
   */
  public RouteDefinition addConsumes(String contentType) {
    assertNonEmptyContentType(contentType);
    consumes.add(contentType);
    return this;
  }
  
  public Collection<String> produces() {
    if (produces.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(produces);
  }
  
  /**
   * Can be called multiple times to add multiple content types
   */
  public RouteDefinition addProduces(String contentType) {
    assertNonEmptyContentType(contentType);
    produces.add(contentType);
    return this;
  }
  
  public Handler<RoutingContext> handler() {
    if (handler == null) {
      throw new IllegalStateException("No handlers were set");
    }
    return handler;
  }
  
  public RouteDefinition handler(Handler<RoutingContext> handler) {
    Objects.requireNonNull(handler, "Handler cannot be null");
    this.handler = handler;
    return this;
  }
  
  public HandlerMode handlerMode() {
    return handlerMode;
  }
  
  public RouteDefinition handlerMode(HandlerMode handlerMode) {
    Objects.requireNonNull(handlerMode, "handler mode cannot be null");
    this.handlerMode = handlerMode;
    return this;
  }
}
