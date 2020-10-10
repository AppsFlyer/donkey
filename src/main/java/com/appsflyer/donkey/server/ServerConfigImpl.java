/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.route.RouteList;
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
  private RouteList routeList;
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
  public RouteList routeList() {
    return routeList;
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
    public ServerConfigBuilder routeList(RouteList routeList) {
      instance.routeList = routeList;
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
      Objects.requireNonNull(instance.routeList, "Router definition list is missing");
      if (instance.instances < 1) {
        throw new IllegalArgumentException("Number of instances must be greater than 0");
      }
    }
  }
  
}
