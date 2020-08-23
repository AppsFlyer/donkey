package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.RouteDescriptor;

import java.util.List;
import java.util.Objects;

public class RouterDefinition {
  private final List<RouteDescriptor> routes;
  private final Middleware middleware;
  
  public RouterDefinition(List<RouteDescriptor> routes) {
    this(routes, null);
  }
  
  public RouterDefinition(List<RouteDescriptor> routes, Middleware middleware) {
    
    Objects.requireNonNull(routes, "Routes cannot be null");
    if (routes.isEmpty()) {
      throw new IllegalArgumentException("Routes cannot be empty");
    }
    
    this.routes = List.copyOf(routes);
    this.middleware = middleware;
  }
  
  public List<RouteDescriptor> routes() {
    return routes;
  }
  
  public Middleware middleware() {
    return middleware;
  }
  
  
}
