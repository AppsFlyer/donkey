package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Objects;

public class ClientConfigImpl implements ClientConfig {
  
  private Vertx vertx;
  private HttpClientOptions clientOptions;
  private WebClientOptions webClientOptions;
  private boolean debug;
  
  @Override
  public Vertx vertx() {
    return vertx;
  }
  
  @Override
  public HttpClientOptions clientOptions() {
    return clientOptions;
  }
  
  @Override
  public WebClientOptions webClientOptions() {
    return webClientOptions;
  }
  
  @Override
  public boolean debug() {
    return debug;
  }
  
  public static final class ClientConfigBuilderImpl implements ClientConfigBuilder {
    
    private ClientConfigImpl instance;
    
    ClientConfigBuilderImpl() {
      instance = new ClientConfigImpl();
    }
    
    @Override
    public ClientConfigBuilder vertx(Vertx vertx) {
      Objects.requireNonNull(vertx, "Vertx argument cannot be null");
      instance.vertx = vertx;
      return this;
    }
    
    @Override
    public ClientConfigBuilder clientOptions(HttpClientOptions clientOptions) {
      Objects.requireNonNull(clientOptions, "Client options argument cannot be null");
      instance.clientOptions = clientOptions;
      return this;
    }
    
    @Override
    public ClientConfigBuilder webClientOptions(WebClientOptions webClientOptions) {
      Objects.requireNonNull(webClientOptions, "Web client options argument cannot be null");
      instance.webClientOptions = webClientOptions;
      return this;
    }
    
    @Override
    public ClientConfigBuilder debug(boolean val) {
      instance.debug = val;
      return this;
    }
    
    @Override
    public ClientConfig build() {
      assertValidState();
      var res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState() {
      Objects.requireNonNull(instance.vertx, "Vertx field is missing");
      Objects.requireNonNull(instance.clientOptions, "Client options field is missing");
      Objects.requireNonNull(instance.webClientOptions, "Web client options field is missing");
      
    }
  }
}
