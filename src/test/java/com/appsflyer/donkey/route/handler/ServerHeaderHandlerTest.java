package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.appsflyer.donkey.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ServerHeaderHandlerTest {
  
  private Server server;
  
  @AfterEach
  void tearDown() throws ServerShutdownException {
    server.shutdownSync();
    server = null;
  }
  
  @Test
  void testDateHeaderNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) {
    server = Server.create(getDefaultConfigBuilder(vertx).build());
    server.start()
          .onSuccess(
              v -> doGet(vertx, "/")
                  .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assert200(response);
                    assertNull(response.getHeader("Server"));
                    testContext.completeNow();
                  }))));
  }
  
  @Test
  void testAddingDateHeader(Vertx vertx, VertxTestContext testContext) {
    server = Server.create(getDefaultConfigBuilder(vertx).addServerHeader(true).build());
    server.start()
          .onSuccess(
              v -> doGet(vertx, "/")
                  .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assert200(response);
                    assertEquals(ServerHeaderHandler.SERVER_NAME, response.getHeader("Server"));
                    testContext.completeNow();
                  }))));
  }
}
