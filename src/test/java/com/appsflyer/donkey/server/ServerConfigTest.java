package com.appsflyer.donkey.server;

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
    var routeCreatorSupplier = new RingRouteCreatorSupplier();
    var routerDefinition = new RouterDefinition(List.of(RouteDescriptor.create()));
    
    assertDoesNotThrow(() -> new ServerConfig(vertxOptions, serverOptions, routeCreatorSupplier, routerDefinition));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(null, serverOptions, routeCreatorSupplier, routerDefinition));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, null, routeCreatorSupplier, routerDefinition));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, serverOptions, routeCreatorSupplier, null));
  }
}
