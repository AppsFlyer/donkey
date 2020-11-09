/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey.server.handler;

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
