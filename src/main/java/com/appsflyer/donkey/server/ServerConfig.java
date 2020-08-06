package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;

import java.util.List;
import java.util.Objects;

public class ServerConfig
{
  private final HttpServerOptions serverOptions;
  private final VertxOptions vertxOptions;
  private final List<RouteDescriptor> routes;
  private final HandlerFactory handlerFactory;
  
  public ServerConfig(
      VertxOptions vertxOptions,
      HttpServerOptions serverOptions,
      List<RouteDescriptor> routes,
      HandlerFactory handlerFactory)
  {
    Objects.requireNonNull(vertxOptions, "Vert.x options is missing");
    Objects.requireNonNull(serverOptions, "Server options is missing");
    Objects.requireNonNull(routes, "Routes list is missing");
    Objects.requireNonNull(handlerFactory, "Handler factory is missing");
    
    this.serverOptions = serverOptions;
    this.routes = List.copyOf(routes);
    this.handlerFactory = handlerFactory;
    this.vertxOptions = vertxOptions;
  }
  
  HttpServerOptions serverOptions()
  {
    return serverOptions;
  }
  
  public List<RouteDescriptor> routes()
  {
    return routes;
  }
  
  HandlerFactory handlerFactory()
  {
    return handlerFactory;
  }
  
  VertxOptions vertxOptions()
  {
    return vertxOptions;
  }
  
}
