package com.appsflyer.donkey.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.appsflyer.donkey.exception.ServerInitializationException;
import com.appsflyer.donkey.route.handler.HandlerFactoryStub;
import com.appsflyer.donkey.route.RouteDescriptor;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.util.List;
import java.util.function.Function;

import static com.appsflyer.donkey.TestUtil.getDefaultAddress;
import static io.vertx.core.http.HttpMethod.GET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ServerTest
{
  private static final int port = 16969;
  private static final String responseBody = "Hello world";
  
  @Test
  void testServerAsyncLifecycle(Vertx vertx, VertxTestContext testContext)
  {
    Server server = new Server(newServerConfig(newRouteDescriptor()));
    server.start()
          .onComplete(startResult -> {
            if (startResult.failed()) {
              testContext.failNow(startResult.cause());
            }
      
            WebClient.create(vertx)
                     .request(GET, getDefaultAddress(), "/")
                     .send(testContext.succeeding(response -> testContext.verify(() -> {
                       assertEquals(200, response.statusCode());
                       assertEquals(responseBody, response.bodyAsString());
        
                       server.shutdown().onComplete(stopResult -> {
                         if (stopResult.failed()) {
                           testContext.failNow(stopResult.cause());
                         }
                         testContext.completeNow();
                       });
                     })));
          });
  }
  
  @Test
  void testServerSyncLifecycle(Vertx vertx, VertxTestContext testContext) throws
                                                                          ServerInitializationException
  {
    Server server = new Server(newServerConfig(newRouteDescriptor()));
    server.startSync();
    
    WebClient.create(vertx)
             .request(GET, getDefaultAddress(), "/")
             .send(testContext.succeeding(response -> testContext.verify(() -> {
               assertEquals(200, response.statusCode());
               assertEquals(responseBody, response.bodyAsString());
               server.shutdownSync();
               testContext.completeNow();
             })));
  }
  
  @Test
  void testAddressAlreadyInUse(Vertx vertx, VertxTestContext testContext) throws
                                                                          Exception
  {
    // Because we are testing an exception we turn off the logging so they don't show up in the test output
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger serverLogger = loggerContext.getLogger(ServerVerticle.class.getPackageName());
    Level originalLevel = serverLogger.getLevel();
    serverLogger.setLevel(Level.OFF);
    
    Server server1 = new Server(newServerConfig(newRouteDescriptor()));
    server1.startSync();
    
    Server server2 = new Server(newServerConfig(newRouteDescriptor()));
    assertThrows(ServerInitializationException.class, server2::startSync);
    
    Server server3 = new Server(newServerConfig(newRouteDescriptor()));
    server3.start().onComplete(testContext.failing(ex -> testContext.verify(() -> {
      assertThat(ex, instanceOf(BindException.class));
      serverLogger.setLevel(originalLevel);
      server1.shutdownSync();
      testContext.completeNow();
    })));
  }
  
  private RouteDescriptor newRouteDescriptor()
  {
    RouteDescriptor routeDescriptor = mock(RouteDescriptor.class);
    Function<RoutingContext, ?> handler = ctx -> {
      ctx.response().end(responseBody);
      return null;
    };
    doReturn(handler).when(routeDescriptor).handler();
    return routeDescriptor;
  }
  
  private ServerConfig newServerConfig(RouteDescriptor routeDescriptor)
  {
    return new ServerConfig(
        new VertxOptions().setEventLoopPoolSize(1),
        new HttpServerOptions().setPort(port),
        List.of(routeDescriptor),
        new HandlerFactoryStub());
  }
}
