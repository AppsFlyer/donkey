package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.router.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

/**
 * A DTO object used for initializing a {@link Server}.
 * Use {@link ServerConfigBuilder} to create instances.
 */
public interface ServerConfig {
  
  static ServerConfigBuilder builder() {
    return ServerConfigBuilder.create();
  }
  
  /**
   * Get the {@code Vertx} instance associated with the server.
   * Every {@link Server} and {@link com.appsflyer.donkey.client.Client}
   * associated with this {@code Vertx} instance will share its resources, mainly
   * the event loops.
   */
  Vertx vertx();
  
  /**
   * @return Vertx server configuration.
   * @see HttpServerOptions
   */
  HttpServerOptions serverOptions();
  
  /**
   * @return A factory of
   * {@link com.appsflyer.donkey.server.route.RouteCreator}s
   */
  RouteCreatorFactory routeCreatorFactory();
  
  /**
   * @return an object the encapsulates the definition for creating a
   * {@link io.vertx.ext.web.Router}
   */
  RouterDefinition routerDefinition();
  
  /**
   * @return The number of server verticals to deploy
   */
  int instances();
  
  /**
   * @return Whether debug mode is enabled or not.
   */
  boolean debug();
  
  /**
   * @return Whether to add the "Date" header to responses or not.
   */
  boolean addDateHeader();
  
  /**
   * @return Whether to try and add a "Content-Type" header to responses or not.
   */
  boolean addContentTypeHeader();
  
  /**
   * @return Whether to add the "Server" header to responses or not.
   */
  boolean addServerHeader();
}
