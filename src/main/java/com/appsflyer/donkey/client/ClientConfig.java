package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClientOptions;

public interface ClientConfig {
  
  static ClientConfigBuilder builder() {
    return ClientConfigBuilder.create();
  }
  
  Vertx vertx();
  
  WebClientOptions clientOptions();
  
  boolean debug();
}
