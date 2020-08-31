package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import com.appsflyer.donkey.route.ring.RingRouteCreatorSupplier;
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
    RouteCreatorSupplier routeCreatorSupplier = new RingRouteCreatorSupplier();
    var routerDefinition = RouterDefinition.from(RouteDescriptor.create());
  
    assertDoesNotThrow(() -> ServerConfig.builder()
                                         .vertx(vertx)
                                         .instances(1)
                                         .serverOptions(serverOptions)
                                         .routeCreatorSupplier(routeCreatorSupplier)
                                         .routerDefinition(routerDefinition)
                                         .build());
  
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(null)
                                   .instances(1)
                                   .serverOptions(serverOptions)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(routerDefinition)
                                   .build());
  
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(null)
                                   .instances(0)
                                   .serverOptions(serverOptions)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(vertx)
                                   .instances(1)
                                   .serverOptions(null)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertx(vertx)
                                   .instances(1)
                                   .serverOptions(serverOptions)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(null)
                                   .build());
  }
}
