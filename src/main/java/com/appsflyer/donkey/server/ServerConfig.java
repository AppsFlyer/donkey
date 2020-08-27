package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

public interface ServerConfig {
  
  static ServerConfigBuilder builder() {
    return ServerConfigBuilder.create();
  }
  
  VertxOptions vertxOptions();
  
  HttpServerOptions serverOptions();
  
  RouteCreatorSupplier routeFactoryCreator();
  
  RouterDefinition routerDefinition();
  
  boolean debug();
  
  boolean addDateHeader();
  
  boolean addContentTypeHeader();
  
  boolean addServerHeader();
}
