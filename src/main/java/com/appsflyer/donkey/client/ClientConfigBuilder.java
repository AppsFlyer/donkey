package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClientOptions;

public interface ClientConfigBuilder {
  
  static ClientConfigBuilder create() {
    return new ClientConfigImpl.ClientConfigBuilderImpl();
  }
  
  ClientConfigBuilder vertx(Vertx vertx);
  
  ClientConfigBuilder clientOptions(WebClientOptions clientOptions);
  
  ClientConfigBuilder debug(boolean val);
  
  ClientConfig build();
}
