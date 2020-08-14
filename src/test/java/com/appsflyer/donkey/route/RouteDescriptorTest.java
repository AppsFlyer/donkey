package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.ring.RingRouteDescriptor;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.vertx.core.http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;

class RouteDescriptorTest
{
  @Test
  void testRequiredHandler()
  {
    RouteDescriptor descriptor = new RingRouteDescriptor();
    assertThrows(IllegalStateException.class, descriptor::handlers);
  }
  
  @Test
  void testNonNullArguments()
  {
    RouteDescriptor descriptor = new RingRouteDescriptor();
    assertThrows(NullPointerException.class, () -> descriptor.addMethod(null));
    assertThrows(NullPointerException.class, () -> descriptor.addConsumes(null));
    assertThrows(NullPointerException.class, () -> descriptor.addProduces(null));
    assertThrows(NullPointerException.class, () -> descriptor.handlerMode(null));
    assertThrows(NullPointerException.class, () -> descriptor.addHandler(null));
    
    assertDoesNotThrow(() -> descriptor.path(null));
  }
  
  @Test
  void testDefaultValues()
  {
    Handler<RoutingContext> handler = v -> {};
    RouteDescriptor descriptor = new RingRouteDescriptor().addHandler(handler);
    assertNull(descriptor.path());
    assertEquals(Collections.emptySet(), descriptor.methods());
    assertEquals(Collections.emptySet(), descriptor.consumes());
    assertEquals(Collections.emptySet(), descriptor.produces());
    assertEquals(List.of(handler), descriptor.handlers());
    assertEquals(HandlerMode.NON_BLOCKING, descriptor.handlerMode());
  }
  
  @Test
  void testMethods()
  {
    RouteDescriptor descriptor = new RingRouteDescriptor().addMethod(POST);
    assertEquals(Set.of(POST), descriptor.methods());
    
    descriptor = new RingRouteDescriptor()
        .addMethod(GET)
        .addMethod(POST)
        .addMethod(PUT)
        .addMethod(DELETE);
    assertEquals(Set.of(GET, POST, PUT, DELETE), descriptor.methods());
  }
  
  @Test
  void testConsumes()
  {
    RouteDescriptor descriptor = new RingRouteDescriptor().addConsumes("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.consumes());
    
    descriptor = new RingRouteDescriptor()
        .addConsumes("text/plain")
        .addConsumes("application/json")
        .addConsumes("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 descriptor.consumes());
    
  }
  
  @Test
  void testProduces()
  {
    RouteDescriptor descriptor = new RingRouteDescriptor().addProduces("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.produces());
    
    descriptor = new RingRouteDescriptor()
        .addProduces("text/plain")
        .addProduces("application/json")
        .addProduces("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 descriptor.produces());
    
  }
}
