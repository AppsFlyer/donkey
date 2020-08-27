package com.appsflyer.donkey.route;

import java.util.*;

public class RouterDefinition {
  
  private final Deque<RouteDescriptor> routes;
  
  public RouterDefinition(List<RouteDescriptor> routes) {
    
    Objects.requireNonNull(routes, "Routes cannot be null");
    if (routes.isEmpty()) {
      throw new IllegalArgumentException("Routes cannot be empty");
    }
    
    this.routes = new LinkedList<>(routes);
  }
  
  public List<RouteDescriptor> routes() {
    return List.copyOf(routes);
  }
  
  public RouterDefinition addFirst(RouteDescriptor rd) {
    Objects.requireNonNull(rd, "Route descriptor cannot be null");
    routes.addFirst(rd);
    return this;
  }
  
  public RouterDefinition addLast(RouteDescriptor rd) {
    Objects.requireNonNull(rd, "Route descriptor cannot be null");
    routes.addLast(rd);
    return this;
  }
}
