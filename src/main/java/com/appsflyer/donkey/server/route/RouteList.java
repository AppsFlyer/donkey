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

import java.util.*;

/**
 * The class represents a logical container for one or more {@link RouteDefinition}.
 */
public class RouteList {
  
  private final Deque<RouteDefinition> routes;
  
  public static RouteList from(RouteDefinition... routes) {
    return new RouteList(List.of(routes));
  }
  
  public RouteList(List<RouteDefinition> routes) {
    Objects.requireNonNull(routes, "Routes cannot be null");
    if (routes.isEmpty()) {
      throw new IllegalArgumentException("Routes cannot be empty");
    }
    
    this.routes = new LinkedList<>(routes);
  }
  
  public List<RouteDefinition> routes() {
    return List.copyOf(routes);
  }
  
  public RouteList addFirst(RouteDefinition rd) {
    Objects.requireNonNull(rd, "Route definition cannot be null");
    routes.addFirst(rd);
    return this;
  }
  
  public RouteList addLast(RouteDefinition rd) {
    Objects.requireNonNull(rd, "Route definition cannot be null");
    routes.addLast(rd);
    return this;
  }
}
