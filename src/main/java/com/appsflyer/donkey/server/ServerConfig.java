package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

import java.util.Objects;

//@todo fix doc string

/**
 * A DTO object used for initializing a {@link ServerImpl}.
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
 */
public class ServerConfig {
  
  private final VertxOptions vertxOptions;
  private final HttpServerOptions serverOptions;
  private final RouteCreatorSupplier routeCreatorSupplier;
  private final RouterDefinition routerDefinition;
  
  public ServerConfig(
      VertxOptions vertxOptions,
      HttpServerOptions serverOptions,
      RouteCreatorSupplier routeCreatorSupplier,
      RouterDefinition routerDefinition) {
    
    Objects.requireNonNull(vertxOptions, "Vert.x options is missing");
    Objects.requireNonNull(serverOptions, "Server options is missing");
    Objects.requireNonNull(routeCreatorSupplier, "Route creator supplier is missing");
    Objects.requireNonNull(routerDefinition, "Router definition is missing");
    
    this.vertxOptions = vertxOptions;
    this.serverOptions = serverOptions;
    this.routeCreatorSupplier = routeCreatorSupplier;
    this.routerDefinition = routerDefinition;
  }
  
  VertxOptions vertxOptions() {
    return vertxOptions;
  }
  
  HttpServerOptions serverOptions() {
    return serverOptions;
  }
  
  public RouteCreatorSupplier routeFactoryCreator() {
    return routeCreatorSupplier;
  }
  
  RouterDefinition routerDefinition() {
    return routerDefinition;
  }
  
}
