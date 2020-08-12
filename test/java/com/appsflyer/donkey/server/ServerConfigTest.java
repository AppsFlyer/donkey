package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.HandlerFactoryStub;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerConfigTest
{
  
  @Test
  void testRequiredOptions()
  {
    var vertxOptions = new VertxOptions();
    var serverOptions = new HttpServerOptions();
    List<RouteDescriptor> routeDescriptors = List.of();
    HandlerFactory handlerFactory = new HandlerFactoryStub();
    
    assertDoesNotThrow(() -> new ServerConfig(vertxOptions, serverOptions, routeDescriptors, handlerFactory));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(null, serverOptions, routeDescriptors, handlerFactory));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, null, routeDescriptors, handlerFactory));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, serverOptions, null, handlerFactory));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, serverOptions, routeDescriptors, null));
  }
}
