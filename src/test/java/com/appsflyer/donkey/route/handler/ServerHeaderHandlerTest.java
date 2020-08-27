package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.server.Server;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.appsflyer.donkey.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ServerHeaderHandlerTest {
  
  @Test
  void testDateHeaderNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) {
    var server = Server.create(getDefaultConfigBuilder().build());
    server.start();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          server.shutdown();
          assert200(response);
          assertNull(response.getHeader("Server"));
          testContext.completeNow();
        })));
  }
  
  @Test
  void testAddingDateHeader(Vertx vertx, VertxTestContext testContext) {
    var server = Server.create(getDefaultConfigBuilder().addServerHeader(true).build());
    server.start();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          server.shutdown();
          assert200(response);
          assertEquals(ServerHeaderHandler.SERVER_NAME, response.getHeader("Server"));
          testContext.completeNow();
        })));
    
  }
}
