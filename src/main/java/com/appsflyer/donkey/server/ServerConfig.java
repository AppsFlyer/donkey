package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

public interface ServerConfig {
  
  static ServerConfigBuilder builder() {
    return ServerConfigBuilder.create();
  }
  
  Vertx vertx();
  
  HttpServerOptions serverOptions();
  
  RouteCreatorSupplier routeFactoryCreator();
  
  RouterDefinition routerDefinition();
  
  int instances();
  
  boolean debug();
  
  boolean addDateHeader();
  
  boolean addContentTypeHeader();
  
  boolean addServerHeader();
}
