package com.appsflyer.donkey.route;

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
    RouteDescriptor descriptor = RouteDescriptor.create();
    assertThrows(IllegalStateException.class, descriptor::handler);
  }
  
  @Test
  void testNonNullArguments()
  {
    RouteDescriptor descriptor = RouteDescriptor.create();
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
    RouteDescriptor descriptor = RouteDescriptor.create().addHandler(handler);
    assertNull(descriptor.path());
    assertEquals(Collections.emptySet(), descriptor.methods());
    assertEquals(Collections.emptySet(), descriptor.consumes());
    assertEquals(Collections.emptySet(), descriptor.produces());
    assertEquals(List.of(handler), descriptor.handler());
    assertEquals(HandlerMode.NON_BLOCKING, descriptor.handlerMode());
  }
  
  @Test
  void testMethods()
  {
    RouteDescriptor descriptor = RouteDescriptor.create().addMethod(POST);
    assertEquals(Set.of(POST), descriptor.methods());
    
    descriptor = RouteDescriptor.create()
        .addMethod(GET)
        .addMethod(POST)
        .addMethod(PUT)
        .addMethod(DELETE);
    assertEquals(Set.of(GET, POST, PUT, DELETE), descriptor.methods());
  }
  
  @Test
  void testConsumes()
  {
    RouteDescriptor descriptor = RouteDescriptor.create().addConsumes("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.consumes());
    
    descriptor = RouteDescriptor.create()
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
    RouteDescriptor descriptor = RouteDescriptor.create().addProduces("text/plain");
    assertEquals(Set.of("text/plain"), descriptor.produces());
    
    descriptor = RouteDescriptor.create()
        .addProduces("text/plain")
        .addProduces("application/json")
        .addProduces("application/x-www-form-urlencoded");
    
    assertEquals(Set.of("text/plain",
                        "application/json",
                        "application/x-www-form-urlencoded"),
                 descriptor.produces());
    
  }
}
