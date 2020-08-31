package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClientOptions;

public interface ClientConfig {
  
  static ClientConfigBuilder builder() {
    return ClientConfigBuilder.create();
  }
  
  Vertx vertx();
  
  HttpClientOptions clientOptions();
  
  WebClientOptions webClientOptions();
  
  boolean debug();
}
