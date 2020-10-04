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
    RouteDefinition descriptor = RouteDefinition.create();
    assertThrows(IllegalStateException.class, descriptor::handler);
  }
  
  @Test
  void testNonNullArguments() {
    RouteDefinition descriptor = RouteDefinition.create();
    assertThrows(NullPointerException.class, () -> descriptor.addMethod(null));
    assertThrows(NullPointerException.class, () -> descriptor.addConsumes(null));
    assertThrows(NullPointerException.class, () -> descriptor.addProduces(null));
    assertThrows(NullPointerException.class, () -> descriptor.handlerMode(null));
    assertThrows(NullPointerException.class, () -> descriptor.handler(null));
    
    assertDoesNotThrow(() -> descriptor.path(null));
  }
  
  @Test
  void testDefaultValues() {
    Handler<RoutingContext> handler = v -> {};
    RouteDefinition descriptor = RouteDefinition.create().handler(handler);
    assertNull(descriptor.path());
    assertEquals(Collections.emptySet(), descriptor.methods());
    assertEquals(Collections.emptySet(), descriptor.consumes());
    assertEquals(Collections.emptySet(), descriptor.produces());
    assertEquals(handler, descriptor.handler());
    assertEquals(HandlerMode.NON_BLOCKING, descriptor.handlerMode());
  }
  
  @Test
  void testMethods() {
    RouteDefinition descriptor = RouteDefinition.create().addMethod(POST);
    assertEquals(Set.of(POST), descriptor.methods());
  
    descriptor = RouteDefinition.create()
                                .addMethod(GET)
                                .addMethod(POST)
                                .addMethod(PUT)
                                .addMethod(DELETE);
    assertEquals(Set.of(GET, POST, PUT, DELETE), descriptor.methods());
  }
  
  @Test
  void testConsumes() {
    RouteDefinition descriptor = RouteDefinition.create().addConsumes("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.consumes());
  
    descriptor = RouteDefinition.create()
                                .addConsumes("text/plain")
                                .addConsumes("application/json")
                                .addConsumes("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 descriptor.consumes());
  }
  
  @Test
  void testProduces() {
    RouteDefinition descriptor = RouteDefinition.create().addProduces("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.produces());
  
    descriptor = RouteDefinition.create()
                                .addProduces("text/plain")
                                .addProduces("application/json")
                                .addProduces("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 descriptor.produces());
  }
  
  @Test
  void testContentTypeCannotBeBlank() {
    RouteDefinition descriptor = RouteDefinition.create();
    Stream.of("", " ").forEach(type -> {
      assertThrows(IllegalArgumentException.class, () -> descriptor.addConsumes(type));
      assertThrows(IllegalArgumentException.class, () -> descriptor.addProduces(type));
    });
  }
}
