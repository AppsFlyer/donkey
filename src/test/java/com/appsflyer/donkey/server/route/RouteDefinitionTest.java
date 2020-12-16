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

package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static io.vertx.core.http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;

class RouteDefinitionTest {
  
  @Test
  void testRequiredHandler() {
    RouteDefinition routeDefinition = RouteDefinition.create();
    assertThrows(IllegalStateException.class, routeDefinition::handler);
  }
  
  @Test
  void testNonNullArguments() {
    RouteDefinition routeDefinition = RouteDefinition.create();
    assertThrows(NullPointerException.class, () -> routeDefinition.addMethod(null));
    assertThrows(NullPointerException.class, () -> routeDefinition.addConsumes(null));
    assertThrows(NullPointerException.class, () -> routeDefinition.addProduces(null));
    assertThrows(NullPointerException.class, () -> routeDefinition.handlerMode(null));
    assertThrows(NullPointerException.class, () -> routeDefinition.handler(null));
    
    assertDoesNotThrow(() -> routeDefinition.path((String) null));
    assertDoesNotThrow(() -> routeDefinition.path((PathDefinition) null));
  }
  
  @Test
  void testDefaultValues() {
    Handler<RoutingContext> handler = v -> {};
    RouteDefinition routeDefinition = RouteDefinition.create().handler(handler);
    assertNull(routeDefinition.path());
    assertEquals(Collections.emptySet(), routeDefinition.methods());
    assertEquals(Collections.emptySet(), routeDefinition.consumes());
    assertEquals(Collections.emptySet(), routeDefinition.produces());
    assertEquals(handler, routeDefinition.handler());
    assertEquals(HandlerMode.NON_BLOCKING, routeDefinition.handlerMode());
  }
  
  @Test
  void testMethods() {
    RouteDefinition routeDefinition = RouteDefinition.create().addMethod(POST);
    assertEquals(Set.of(POST), routeDefinition.methods());
    
    routeDefinition = RouteDefinition.create()
                                      .addMethod(GET)
                                      .addMethod(POST)
                                      .addMethod(PUT)
                                      .addMethod(DELETE);
    assertEquals(Set.of(GET, POST, PUT, DELETE), routeDefinition.methods());
  }
  
  @Test
  void testConsumes() {
    RouteDefinition routeDefinition = RouteDefinition.create().addConsumes("text/plain");
    assertEquals(Set.of("text/plain"), routeDefinition.consumes());
    
    routeDefinition = RouteDefinition.create()
                                      .addConsumes("text/plain")
                                      .addConsumes("application/json")
                                      .addConsumes("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 routeDefinition.consumes());
  }
  
  @Test
  void testProduces() {
    RouteDefinition routeDefinition = RouteDefinition.create().addProduces("text/plain");
    assertEquals(Set.of("text/plain"), routeDefinition.produces());
    
    routeDefinition = RouteDefinition.create()
                                      .addProduces("text/plain")
                                      .addProduces("application/json")
                                      .addProduces("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 routeDefinition.produces());
  }
  
  @Test
  void testContentTypeCannotBeBlank() {
    RouteDefinition routeDefinition = RouteDefinition.create();
    Stream.of("", " ").forEach(type -> {
      assertThrows(IllegalArgumentException.class, () -> routeDefinition.addConsumes(type));
      assertThrows(IllegalArgumentException.class, () -> routeDefinition.addProduces(type));
    });
  }
}
