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
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

import static com.appsflyer.donkey.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class DateHeaderHandlerTest {
  
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME;
  private Server server;
  
  @AfterEach
  void tearDown() throws ServerShutdownException {
    server.shutdownSync();
    server = null;
  }
  
  @Test
  void testDateHeaderNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) throws
                                                                                     ServerInitializationException {
    server = Server.create(getDefaultConfigBuilder(vertx).build());
    server.startSync();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          assertNull(response.getHeader("Date"));
          testContext.completeNow();
        })));
  }
  
  @Test
  void testAddingDateHeader(Vertx vertx, VertxTestContext testContext) throws
                                                                       ServerInitializationException {
    server = Server.create(getDefaultConfigBuilder(vertx)
                               .addDateHeader(true)
                               .build());
    server.startSync();
    
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    AtomicReference<String> lastDate = new AtomicReference<>("");
    Handler<Long> testCase = v -> doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          // Assert that the Date header is a valid date, and that it changed
          //since the last request.
          String date = response.getHeader("Date");
          assertNotEquals(lastDate.getAndSet(date), date);
          assertValidDate(date);
          responsesReceived.flag();
        })));
    
    //Call one time now
    testCase.handle(0L);
    
    //Call again in one second
    vertx.setTimer(1000, testCase);
  }
  
  private void assertValidDate(CharSequence date) {
    assertDoesNotThrow(() -> DATE_FORMAT.parse(date));
  }
}
