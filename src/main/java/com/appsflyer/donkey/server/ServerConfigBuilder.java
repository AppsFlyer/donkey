package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.router.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

public interface ServerConfigBuilder {
  
  static ServerConfigBuilder create() {
    return new ServerConfigImpl.ServerConfigBuilderImpl();
  }
  
  ServerConfigBuilder vertx(Vertx vertx);
  
  ServerConfigBuilder serverOptions(HttpServerOptions serverOptions);
  
  ServerConfigBuilder routeCreatorFactory(RouteCreatorFactory routeCreatorFactory);
  
  ServerConfigBuilder routerDefinition(RouterDefinition routerDefinition);
  
  ServerConfigBuilder instances(int val);
  
  ServerConfigBuilder debug(boolean val);
  
  ServerConfigBuilder addDateHeader(boolean val);
  
  ServerConfigBuilder addContentTypeHeader(boolean val);
  
  ServerConfigBuilder addServerHeader(boolean val);
  
  ServerConfig build();
}
