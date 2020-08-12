package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

import java.util.List;
import java.util.Objects;

//@todo Add routes description
/**
 * A DTO object used for initializing a {@link Server}.
 * It is comprised of the following building blocks:
 * <br><br>
 * <p>
 * {@link VertxOptions} vertxOptions - Used to configure and control the {@link io.vertx.core.Vertx}
 * instance associated with the server. See the
 * <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">documentation</a>
 * for the different options.
 * <br><br>
 * <p>
 * {@link HttpServerOptions} serverOptions - Used to configure HTTP related settings of the server.
 * See the
 * <a href="https://vertx.io/docs/apidocs/io/vertx/core/http/HttpServerOptions.html">documentation</a>
 * for the different options.
 * <br><br>
 * <p>
 *
 * <br><br>
 * <p>
 * {@link HandlerFactory} handlerFactory - Used to create handlers for processing requests / responses.
 */
public class ServerConfig
{
  private final VertxOptions vertxOptions;
  private final HttpServerOptions serverOptions;
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
