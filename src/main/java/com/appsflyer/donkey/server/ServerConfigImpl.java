package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

import java.util.Objects;

//@todo fix doc string

/**
 * A DTO object used for initializing a {@link ServerImpl}.
 * It is comprised of the following building blocks:
 * <br><br>
 * <p>
 * {@link VertxOptions} vertxOptions - Used to configure and control the {@link io.vertx.core.Vertx}
 * instance associated with the server. See the
 * <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">documentation</a>
 * for the different options.
 * <br><br>
 * <p>
 * {@link HttpServerOptions} serverOptions - Used to configure HTTP related settings of the server.
 * See the
 * <a href="https://vertx.io/docs/apidocs/io/vertx/core/http/HttpServerOptions.html">documentation</a>
 * for the different options.
 * <br><br>
 * <p>
 *
 * <br><br>
 * <p>
 */
public final class ServerConfigImpl implements ServerConfig {
  
  private VertxOptions vertxOptions;
  private HttpServerOptions serverOptions;
  private RouteCreatorSupplier routeCreatorSupplier;
  private RouterDefinition routerDefinition;
  private boolean debug;
  private boolean addDateHeader;
  private boolean addContentTypeHeader;
  private boolean addServerHeader;
  
  private ServerConfigImpl() {}
  
  @Override
  public VertxOptions vertxOptions() {
    return vertxOptions;
  }
  
  @Override
  public HttpServerOptions serverOptions() {
    return serverOptions;
  }
  
  @Override
  public RouteCreatorSupplier routeFactoryCreator() {
    return routeCreatorSupplier;
  }
  
  @Override
  public RouterDefinition routerDefinition() {
    return routerDefinition;
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
    public ServerConfigBuilder vertxOptions(VertxOptions vertxOptions) {
      instance.vertxOptions = vertxOptions;
      return this;
    }
    
    @Override
    public ServerConfigBuilder serverOptions(HttpServerOptions serverOptions) {
      instance.serverOptions = serverOptions;
      return this;
    }
    
    @Override
    public ServerConfigBuilder routeCreatorSupplier(RouteCreatorSupplier routeCreatorSupplier) {
      instance.routeCreatorSupplier = routeCreatorSupplier;
      return this;
    }
    
    @Override
    public ServerConfigBuilder routerDefinition(RouterDefinition routerDefinition) {
      instance.routerDefinition = routerDefinition;
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
      Objects.requireNonNull(instance.vertxOptions, "Vert.x options is missing");
      Objects.requireNonNull(instance.serverOptions, "Server options is missing");
      Objects.requireNonNull(instance.routeCreatorSupplier, "Route creator supplier is missing");
      Objects.requireNonNull(instance.routerDefinition, "Router definition is missing");
    }
  }
  
}
