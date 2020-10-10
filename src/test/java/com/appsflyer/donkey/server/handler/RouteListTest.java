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

package com.appsflyer.donkey.server.handler;

import com.appsflyer.donkey.server.route.HandlerMode;
import com.appsflyer.donkey.server.route.PathDefinition;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteListTest {
  
  private static RouteDefinition routeDefinition;
  
  @BeforeAll
  static void beforeAll() {
    routeDefinition = RouteDefinition.create()
                                     .path(PathDefinition.create("/"))
                                     .addConsumes("application/json")
                                     .handlerMode(HandlerMode.BLOCKING)
                                     .handler(RoutingContext::next);
  }
  
  @Test
  void testRequiredArgument() {
    assertThrows(NullPointerException.class, () -> new RouteList(null));
    assertThrows(IllegalArgumentException.class, () -> new RouteList(List.of()));
    assertThrows(IllegalArgumentException.class, RouteList::from);
  }
  
  @Test
  void testAddFirst() {
    var newRouteDefinition = RouteDefinition.create().handler(RoutingContext::next);
    var routeList =
        RouteList.from(routeDefinition).addFirst(newRouteDefinition);
    
    assertEquals(2, routeList.routes().size());
    assertEquals(newRouteDefinition, routeList.routes().get(0));
    assertEquals(routeDefinition, routeList.routes().get(1));
  }
  
  @Test
  void testAddLast() {
    var newRouteDefinition = RouteDefinition.create().handler(RoutingContext::next);
    var routerList =
        RouteList.from(routeDefinition).addLast(newRouteDefinition);
  
    assertEquals(2, routerList.routes().size());
    assertEquals(routeDefinition, routerList.routes().get(0));
    assertEquals(newRouteDefinition, routerList.routes().get(1));
  }
  
  @Test
  void testReturnsImmutableRoutesList() {
    List<RouteDefinition> mutableList = new ArrayList<>(1);
    mutableList.add(routeDefinition);
    var routeList = new RouteList(mutableList);
  
    List<RouteDefinition> routeDefinitions = routeList.routes();
    assertEquals(1, routeDefinitions.size());
    assertThrows(UnsupportedOperationException.class, () -> routeDefinitions.remove(0));
  
    //Changing the original list should not affect any other
    mutableList.remove(0);
    assertEquals(1, routeDefinitions.size());
    assertEquals(1, routeList.routes().size());
  }
  
}
