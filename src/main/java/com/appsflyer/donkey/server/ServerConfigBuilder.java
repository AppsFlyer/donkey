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

public interface ServerConfigBuilder {
  
  static ServerConfigBuilder create() {
    return new ServerConfigImpl.ServerConfigBuilderImpl();
  }
  
  ServerConfigBuilder vertx(Vertx vertx);
  
  ServerConfigBuilder serverOptions(HttpServerOptions serverOptions);
  
  ServerConfigBuilder routeCreatorFactory(RouteCreatorFactory routeCreatorFactory);
  
  ServerConfigBuilder routerDefinition(RouteList routeList);
  
  ServerConfigBuilder instances(int val);
  
  ServerConfigBuilder debug(boolean val);
  
  ServerConfigBuilder addDateHeader(boolean val);
  
  ServerConfigBuilder addContentTypeHeader(boolean val);
  
  ServerConfigBuilder addServerHeader(boolean val);
  
  ServerConfig build();
}
