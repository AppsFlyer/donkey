package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.router.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

import java.util.Objects;

/**
 * @see com.appsflyer.donkey.server.ServerConfig
 */
public final class ServerConfigImpl implements ServerConfig {
  
  private Vertx vertx;
  private HttpServerOptions serverOptions;
  private RouteCreatorFactory routeCreatorFactory;
  private RouterDefinition routerDefinition;
  private int instances;
  private boolean debug;
  private boolean addDateHeader;
  private boolean addContentTypeHeader;
  private boolean addServerHeader;
  
  private ServerConfigImpl() {}
  
  @Override
  public Vertx vertx() {
    return vertx;
  }
  
  @Override
  public HttpServerOptions serverOptions() {
    return serverOptions;
  }
  
  @Override
  public RouteCreatorFactory routeCreatorFactory() {
    return routeCreatorFactory;
  }
  
  @Override
  public RouterDefinition routerDefinition() {
    return routerDefinition;
  }
  
  @Override
  public int instances() {
    return instances;
  }
  
  @Override
  public boolean debug() {
    return debug;
  }
  
  @Override
  public boolean addDateHeader() {
    return addDateHeader;
  }
  
  @Override
  public boolean addContentTypeHeader() {
    return addContentTypeHeader;
  }
  
  @Override
  public boolean addServerHeader() {
    return addServerHeader;
  }
  
  public static final class ServerConfigBuilderImpl implements ServerConfigBuilder {
    
    private ServerConfigImpl instance;
    
    ServerConfigBuilderImpl() {
      instance = new ServerConfigImpl();
    }
  
    @Override
    public ServerConfigBuilder vertx(Vertx vertx) {
      instance.vertx = vertx;
      return this;
    }
    
    @Override
    public ServerConfigBuilder serverOptions(HttpServerOptions serverOptions) {
      instance.serverOptions = serverOptions;
      return this;
    }
  
    @Override
    public ServerConfigBuilder routeCreatorFactory(RouteCreatorFactory routeCreatorFactory) {
      instance.routeCreatorFactory = routeCreatorFactory;
      return this;
    }
  
    @Override
    public ServerConfigBuilder routerDefinition(RouterDefinition routerDefinition) {
      instance.routerDefinition = routerDefinition;
      return this;
    }
  
    @Override
    public ServerConfigBuilder instances(int val) {
      instance.instances = val;
      return this;
    }
  
    @Override
    public ServerConfigBuilder debug(boolean val) {
      instance.debug = val;
      return this;
    }
  
    @Override
    public ServerConfigBuilder addDateHeader(boolean val) {
      instance.addDateHeader = val;
      return this;
    }
    
    @Override
    public ServerConfigBuilder addContentTypeHeader(boolean val) {
      instance.addContentTypeHeader = val;
      return this;
    }
    
    @Override
    public ServerConfigBuilder addServerHeader(boolean val) {
      instance.addServerHeader = val;
      return this;
    }
    
    @Override
    public ServerConfig build() {
      assertValidState();
      var res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState() {
      Objects.requireNonNull(instance.vertx, "Vert.x instance is missing");
      Objects.requireNonNull(instance.serverOptions, "Server options is missing");
      Objects.requireNonNull(instance.routeCreatorFactory, "Route creator factory is missing");
      Objects.requireNonNull(instance.routerDefinition, "Router definition is missing");
      if (instance.instances < 1) {
        throw new IllegalArgumentException("Number of instances must be greater than 0");
      }
    }
  }
  
}
