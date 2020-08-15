package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.RouteDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HandlerConfig
{
  private final List<RouteDescriptor> routes;
  private final HandlerFactory handlerFactory;
  private final List<Middleware> middleware;
  
  public HandlerConfig(List<RouteDescriptor> routes,
                       HandlerFactory handlerFactory,
                       List<Middleware> middleware)
  {
    Objects.requireNonNull(routes, "Routes cannot be null");
    if (routes.isEmpty()) {
      throw new IllegalArgumentException("Routes cannot be empty");
    }
    Objects.requireNonNull(handlerFactory, "Handler factory cannot be null");
    
    this.routes = List.copyOf(routes);
    this.handlerFactory = handlerFactory;
    this.middleware = List.copyOf(middleware);
  }
  
  public HandlerConfig(List<RouteDescriptor> routes, HandlerFactory handlerFactory)
  {
    this(routes, handlerFactory, Collections.emptyList());
  }
  
  public List<RouteDescriptor> routes()
  {
    return routes;
  }
  
  public HandlerFactory handlerFactory()
  {
    return handlerFactory;
  }
  
  public List<Middleware> middleware()
  {
    return middleware;
  }
  
}
