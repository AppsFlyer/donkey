package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.RouterDefinition;
import com.appsflyer.donkey.route.ring.RingRouteCreatorSupplier;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.BindException;
import java.util.List;

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
class ServerTest {
  
  private static final int port = 16969;
  private static final String responseBody = "Hello world";
  
  @Test
  void testServerAsyncLifecycle(Vertx vertx, VertxTestContext testContext) {
    Server server = Server.create(newServerConfig(newRouteDescriptor()));
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
                                                                          ServerInitializationException {
    Server server = Server.create(newServerConfig(newRouteDescriptor()));
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
                                                                          Exception {
    Server server1 = Server.create(newServerConfig(newRouteDescriptor()));
    server1.startSync();
    
    Server server2 = Server.create(newServerConfig(newRouteDescriptor()));
    assertThrows(ServerInitializationException.class, server2::startSync);
    
    Server server3 = Server.create(newServerConfig(newRouteDescriptor()));
    server3.start().onComplete(testContext.failing(ex -> testContext.verify(() -> {
      assertThat(ex, instanceOf(BindException.class));
      server1.shutdownSync();
      testContext.completeNow();
    })));
  }
  
  private RouteDescriptor newRouteDescriptor() {
    return RouteDescriptor.create().addHandler(ctx -> ctx.response().end(responseBody));
  }
  
  private ServerConfig newServerConfig(RouteDescriptor routeDescriptor) {
    return new ServerConfig(
        new VertxOptions().setEventLoopPoolSize(1),
        new HttpServerOptions().setPort(port),
        new RingRouteCreatorSupplier(),
        new RouterDefinition(List.of(routeDescriptor)));
  }
}
