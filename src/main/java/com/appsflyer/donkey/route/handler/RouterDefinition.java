package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.RouteDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RouterDefinition {
  private final Collection<RouteDescriptor> routes;
  
  public RouterDefinition(List<RouteDescriptor> routes) {
    
    Objects.requireNonNull(routes, "Routes cannot be null");
    if (routes.isEmpty()) {
      throw new IllegalArgumentException("Routes cannot be empty");
    }
    
    this.routes = List.copyOf(routes);
  }
  
  public Collection<RouteDescriptor> routes() {
    return routes;
  }
  
  
}
