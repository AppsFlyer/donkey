package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.route.RouteDescriptor;
import com.appsflyer.donkey.server.router.RouterDefinition;
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
    var routerDefinition = RouterDefinition.from(RouteDescriptor.create());
  
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
