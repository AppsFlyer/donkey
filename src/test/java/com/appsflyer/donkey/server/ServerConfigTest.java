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
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.router.RouteList;
import com.appsflyer.donkey.server.ring.route.RingRouteCreatorFactory;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerConfigTest {
  
  @Test
  void testRequiredOptions() {
    var vertx = Vertx.vertx();
    var serverOptions = new HttpServerOptions();
    RouteCreatorFactory routeCreatorFactory = new RingRouteCreatorFactory();
    var routerDefinition = RouteList.from(RouteDefinition.create());
  
    assertDoesNotThrow(() -> ServerConfig.builder()
                                         .vertx(vertx)
                                         .instances(1)
                                         .serverOptions(serverOptions)
                                         .routeCreatorFactory(routeCreatorFactory)
                                         .routerDefinition(routerDefinition)
                                         .build());
  
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(null)
                                   .instances(1)
                                   .serverOptions(serverOptions)
                                   .routeCreatorFactory(routeCreatorFactory)
                                   .routerDefinition(routerDefinition)
                                   .build());
  
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(null)
                                   .instances(0)
                                   .serverOptions(serverOptions)
                                   .routeCreatorFactory(routeCreatorFactory)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(vertx)
                                   .instances(1)
                                   .serverOptions(null)
                                   .routeCreatorFactory(routeCreatorFactory)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(vertx)
                                   .instances(1)
                                   .serverOptions(serverOptions)
                                   .routeCreatorFactory(routeCreatorFactory)
                                   .routerDefinition(null)
                                   .build());
  }
}
