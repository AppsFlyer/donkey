package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

public interface ServerConfigBuilder {
  
  static ServerConfigBuilder create() {
    return new ServerConfigImpl.ServerConfigBuilderImpl();
  }
  
  ServerConfigBuilder vertxOptions(VertxOptions vertxOptions);
  
  ServerConfigBuilder serverOptions(HttpServerOptions serverOptions);
  
  ServerConfigBuilder routeCreatorSupplier(RouteCreatorSupplier routeCreatorSupplier);
  
  ServerConfigBuilder routerDefinition(RouterDefinition routerDefinition);
  
  ServerConfigBuilder debug(boolean val);
  
  ServerConfigBuilder addDateHeader(boolean val);
  
  ServerConfigBuilder addContentTypeHeader(boolean val);
  
  ServerConfigBuilder addServerHeader(boolean val);
  
  ServerConfig build();
}
