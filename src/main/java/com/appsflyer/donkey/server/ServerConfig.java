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
import com.appsflyer.donkey.server.router.RouteList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

/**
 * A DTO object used for initializing a {@link Server}.
 * Use {@link ServerConfigBuilder} to create instances.
 */
public interface ServerConfig {
  
  static ServerConfigBuilder builder() {
    return ServerConfigBuilder.create();
  }
  
  /**
   * Get the {@code Vertx} instance associated with the server.
   * Every {@link Server} and {@link com.appsflyer.donkey.client.Client}
   * associated with this {@code Vertx} instance will share its resources, mainly
   * the event loops.
   */
  Vertx vertx();
  
  /**
   * @return Vertx server configuration.
   * @see HttpServerOptions
   */
  HttpServerOptions serverOptions();
  
  /**
   * @return A factory of
   * {@link com.appsflyer.donkey.server.route.RouteCreator}s
   */
  RouteCreatorFactory routeCreatorFactory();
  
  /**
   * @return an object the encapsulates the definition for creating a
   * {@link io.vertx.ext.web.Router}
   */
  RouteList routerDefinition();
  
  /**
   * @return The number of server verticals to deploy
   */
  int instances();
  
  /**
   * @return Whether debug mode is enabled or not.
   */
  boolean debug();
  
  /**
   * @return Whether to add the "Date" header to responses or not.
   */
  boolean addDateHeader();
  
  /**
   * @return Whether to try and add a "Content-Type" header to responses or not.
   */
  boolean addContentTypeHeader();
  
  /**
   * @return Whether to add the "Server" header to responses or not.
   */
  boolean addServerHeader();
}
