/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

import java.util.Objects;

/**
 * Configuration object used for initializing a {@link Server}.
 * Use {@link ServerConfigBuilder} to create instances.
 */
public final class ServerConfig {
  
  public static ServerConfigBuilder builder() {
    return new ServerConfigBuilder();
  }
  
  private Vertx vertx;
  private HttpServerOptions serverOptions;
  private RouteCreatorFactory routeCreatorFactory;
  private RouteList routeList;
  private int instances;
  private boolean debug;
  private boolean addDateHeader;
  private boolean addContentTypeHeader;
  private boolean addServerHeader;
  
  private ServerConfig() {}
  
  public Vertx vertx() {
    return vertx;
  }
  
  public HttpServerOptions serverOptions() {
    return serverOptions;
  }
  
  public RouteCreatorFactory routeCreatorFactory() {
    return routeCreatorFactory;
  }
  
  public RouteList routeList() {
    return routeList;
  }
  
  public int instances() {
    return instances;
  }
  
  public boolean debug() {
    return debug;
  }
  
  public boolean addDateHeader() {
    return addDateHeader;
  }
  
  public boolean addContentTypeHeader() {
    return addContentTypeHeader;
  }
  
  public boolean addServerHeader() {
    return addServerHeader;
  }
  
  public static final class ServerConfigBuilder {
    
    public static ServerConfigBuilder create() {
      return new ServerConfigBuilder();
    }
    
    private ServerConfig instance;
    
    private ServerConfigBuilder() {
      instance = new ServerConfig();
    }
    
    public ServerConfigBuilder vertx(Vertx vertx) {
      instance.vertx = vertx;
      return this;
    }
    
    public ServerConfigBuilder serverOptions(HttpServerOptions serverOptions) {
      instance.serverOptions = serverOptions;
      return this;
    }
    
    public ServerConfigBuilder routeCreatorFactory(RouteCreatorFactory routeCreatorFactory) {
      instance.routeCreatorFactory = routeCreatorFactory;
      return this;
    }
    
    public ServerConfigBuilder routeList(RouteList routeList) {
      instance.routeList = routeList;
      return this;
    }
    
    public ServerConfigBuilder instances(int val) {
      instance.instances = val;
      return this;
    }
    
    public ServerConfigBuilder debug(boolean val) {
      instance.debug = val;
      return this;
    }
    
    public ServerConfigBuilder addDateHeader(boolean val) {
      instance.addDateHeader = val;
      return this;
    }
    
    public ServerConfigBuilder addContentTypeHeader(boolean val) {
      instance.addContentTypeHeader = val;
      return this;
    }
    
    public ServerConfigBuilder addServerHeader(boolean val) {
      instance.addServerHeader = val;
      return this;
    }
    
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
