package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteCreatorSupplier;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import com.appsflyer.donkey.route.ring.RingRouteCreatorSupplier;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerConfigTest {
  
  @Test
  void testRequiredOptions() {
    var vertxOptions = new VertxOptions();
    var serverOptions = new HttpServerOptions();
    RouteCreatorSupplier routeCreatorSupplier = new RingRouteCreatorSupplier();
    var routerDefinition = new RouterDefinition(List.of(RouteDescriptor.create()));
    
    assertDoesNotThrow(() -> ServerConfig.builder()
                                         .vertxOptions(vertxOptions)
                                         .serverOptions(serverOptions)
                                         .routeCreatorSupplier(routeCreatorSupplier)
                                         .routerDefinition(routerDefinition)
                                         .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertxOptions(null)
                                   .serverOptions(serverOptions)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertxOptions(vertxOptions)
                                   .serverOptions(null)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(routerDefinition)
                                   .build());
    
    assertThrows(NullPointerException.class,
                 () -> ServerConfig.builder()
                                   .vertxOptions(vertxOptions)
                                   .serverOptions(serverOptions)
                                   .routeCreatorSupplier(routeCreatorSupplier)
                                   .routerDefinition(null)
                                   .build());
  }
}
