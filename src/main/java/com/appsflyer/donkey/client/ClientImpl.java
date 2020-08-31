package com.appsflyer.donkey.client;

import io.vertx.core.http.HttpClient;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

public class ClientImpl implements Client {
  
  private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class.getName());
  private final ClientConfig config;
  private final HttpClient client;
  private final WebClient webClient;
  
  public ClientImpl(ClientConfig config) {
    this.config = config;
    client = config.vertx().createHttpClient(config.clientOptions());
    webClient = WebClient.wrap(client, config.webClientOptions());
  }
  
  @Override
  public void shutdown() {
    client.close();
  }
}
