package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.handler.HandlerConfig;
import com.appsflyer.donkey.route.handler.HandlerFactoryStub;
import com.appsflyer.donkey.route.ring.RingRouteDescriptor;
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
    HandlerConfig handlerConfig = new HandlerConfig(List.of(new RingRouteDescriptor()), new HandlerFactoryStub());
  
    assertDoesNotThrow(() -> new ServerConfig(vertxOptions, serverOptions, handlerConfig));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(null, serverOptions, handlerConfig));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, null, handlerConfig));
    assertThrows(NullPointerException.class, () ->
        new ServerConfig(vertxOptions, serverOptions, null));
  }
}
