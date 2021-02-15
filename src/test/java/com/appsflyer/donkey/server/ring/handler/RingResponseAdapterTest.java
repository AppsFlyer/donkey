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

package com.appsflyer.donkey.server.ring.handler;

import clojure.lang.Keyword;
import clojure.lang.RT;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.Http1xServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static com.appsflyer.donkey.server.ring.handler.RingHandler.RING_HANDLER_RESULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class RingResponseAdapterTest {
  
  private static final Buffer content = Buffer.buffer("Hello, world!");
  private static String filename;
  
  @BeforeAll
  static void beforeAll(Vertx vertx, VertxTestContext testContext) {
    vertx.fileSystem()
         .createTempFile(null, ".txt")
         .compose(v -> {
           filename = v;
           return vertx.fileSystem().writeFile(v, content);
         })
         .onComplete(testContext.succeedingThenComplete());
  }
  
  Http1xServerResponse mockResponse(RoutingContext ctx, Object expectedResponse) {
    var res = mock(Http1xServerResponse.class);
    when(ctx.get(RING_HANDLER_RESULT))
        .thenReturn(RT.map(
            Keyword.intern("body"), expectedResponse,
            Keyword.intern("headers"), RT.map("cache-control", "no-cache"),
            Keyword.intern("status"), 200));
    when(ctx.response()).thenReturn(res);
    return res;
  }
  
  @Test
  void testServingFileProgrammatically(VertxTestContext testContext) {
    var expectedFile = new File(filename);
    var ctx = mock(RoutingContext.class);
    var res = mockResponse(ctx, expectedFile);
    
    when(res.sendFile(anyString(), anyLong(), anyLong()))
        .thenAnswer(invocationOnMock -> {
          assertEquals(expectedFile.getCanonicalPath(), invocationOnMock.getArgument(0));
          assertEquals(0L, (long) invocationOnMock.getArgument(1));
          assertEquals(content.length(), (long) invocationOnMock.getArgument(2));
          testContext.completeNow();
          return Future.succeededFuture();
        });
    
    RingResponseAdapter.create().handle(ctx);
  }
  
  @Test
  void testNullResponse(VertxTestContext testContext) {
    var res = mock(Http1xServerResponse.class);
    Promise<Void> promise = Promise.promise();
    Future<Void> future = promise.future();
    when(res.end()).thenAnswer(v -> {
      promise.complete();
      return future;
    });
    
    //Mock context that will return a nil Ring response map
    var ctx = mock(RoutingContext.class);
    when(ctx.response()).thenReturn(res);
    
    RingResponseAdapter.create().handle(ctx);
    testContext.assertComplete(future)
               .onComplete(testContext.succeedingThenComplete());
  }
}
