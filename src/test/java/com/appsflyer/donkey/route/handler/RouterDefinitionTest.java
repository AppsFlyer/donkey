package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.HandlerMode;
import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouterDefinitionTest {
  
  private static RouteDescriptor routeDescriptor;
  
  @BeforeAll
  static void beforeAll() {
    routeDescriptor = RouteDescriptor.create()
                                     .path(PathDescriptor.create("/"))
                                     .addConsumes("application/json")
                                     .handlerMode(HandlerMode.BLOCKING)
                                     .handler(RoutingContext::next);
  }
  
  @Test
  void testRequiredArgument() {
    assertThrows(NullPointerException.class, () -> new RouterDefinition(null));
    assertThrows(IllegalArgumentException.class, () -> new RouterDefinition(List.of()));
    assertThrows(IllegalArgumentException.class, RouterDefinition::from);
  }
  
  @Test
  void testAddFirst() {
    var newRouteDescriptor = RouteDescriptor.create().handler(RoutingContext::next);
    var routerDefinition =
        RouterDefinition.from(routeDescriptor).addFirst(newRouteDescriptor);
    
    assertEquals(2, routerDefinition.routes().size());
    assertEquals(newRouteDescriptor, routerDefinition.routes().get(0));
    assertEquals(routeDescriptor, routerDefinition.routes().get(1));
  }
  
  @Test
  void testAddLast() {
    var newRouteDescriptor = RouteDescriptor.create().handler(RoutingContext::next);
    var routerDefinition =
        RouterDefinition.from(routeDescriptor).addLast(newRouteDescriptor);
    
    assertEquals(2, routerDefinition.routes().size());
    assertEquals(routeDescriptor, routerDefinition.routes().get(0));
    assertEquals(newRouteDescriptor, routerDefinition.routes().get(1));
  }
  
  @Test
  void testReturnsImmutableRoutesList() {
    List<RouteDescriptor> mutableList = new ArrayList<>(1);
    mutableList.add(routeDescriptor);
    var routerDefinition = new RouterDefinition(mutableList);
    
    List<RouteDescriptor> routeDescriptors = routerDefinition.routes();
    assertEquals(1, routeDescriptors.size());
    assertThrows(UnsupportedOperationException.class, () -> routeDescriptors.remove(0));
    
    //Changing the original list should not affect any other
    mutableList.remove(0);
    assertEquals(1, routeDescriptors.size());
    assertEquals(1, routerDefinition.routes().size());
  }
  
}
