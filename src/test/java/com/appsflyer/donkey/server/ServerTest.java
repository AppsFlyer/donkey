package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.route.RouteDescriptor;
import com.appsflyer.donkey.server.router.RouterDefinition;
import com.appsflyer.donkey.server.ring.route.RingRouteCreatorFactory;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.appsflyer.donkey.TestUtil.assert200;
import static com.appsflyer.donkey.TestUtil.doGet;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ServerTest {
  
  private static final int port = 16969;
  private static final String responseBody = "Hello world";
  
  private Server server;
  
  @AfterEach
  void tearDown() throws ServerShutdownException {
    if (server != null) {
      server.shutdownSync();
      server = null;
    }
  }
  
  @Test
  void testServerAsyncLifecycle(Vertx vertx, VertxTestContext testContext) {
    server = Server.create(newServerConfig(vertx, newRouteDescriptor()));
    server.start()
          .onFailure(testContext::failNow)
          .onSuccess(startResult -> doGet(vertx, "/")
              .onComplete(testContext.succeeding(
                  response -> testContext.verify(() -> {
                    assert200(response);
                    assertEquals(responseBody, response.bodyAsString());
              
                    server.shutdown().onComplete(stopResult -> {
                      if (stopResult.failed()) {
                        testContext.failNow(stopResult.cause());
                      }
                      testContext.completeNow();
                    });
                  }))));
  }
  
  @Test
  void testServerSyncLifecycle(Vertx vertx, VertxTestContext testContext) throws
                                                                          ServerInitializationException {
    server = Server.create(newServerConfig(vertx, newRouteDescriptor()));
    server.startSync();
  
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(
            response -> testContext.verify(() -> {
              assert200(response);
              assertEquals(responseBody, response.bodyAsString());
              testContext.completeNow();
            })));
  }
  
  private RouteDescriptor newRouteDescriptor() {
    return RouteDescriptor.create().handler(ctx -> ctx.response().end(responseBody));
  }
  
  private ServerConfig newServerConfig(Vertx vertx, RouteDescriptor routeDescriptor) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(4)
                       .serverOptions(new HttpServerOptions().setPort(port))
                       .routeCreatorFactory(new RingRouteCreatorFactory())
                       .routerDefinition(RouterDefinition.from(routeDescriptor))
                       .build();
  }
}
