package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClientOptions;

public interface ClientConfigBuilder {
  
  static ClientConfigBuilder create() {
    return new ClientConfigImpl.ClientConfigBuilderImpl();
  }
  
  ClientConfigBuilder vertx(Vertx vertx);
  
  ClientConfigBuilder clientOptions(HttpClientOptions clientOptions);
  
  ClientConfigBuilder webClientOptions(WebClientOptions webClientOptions);
  
  ClientConfigBuilder debug(boolean val);
  
  ClientConfig build();
}
