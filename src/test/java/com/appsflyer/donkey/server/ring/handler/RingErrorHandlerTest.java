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

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import com.appsflyer.donkey.TestUtil;
import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerImpl;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import com.appsflyer.donkey.server.ring.route.RingRouteSupplier;
import com.appsflyer.donkey.server.route.RouteList;
import com.appsflyer.donkey.server.route.RouteSupplier;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;

import static com.appsflyer.donkey.ClojureObjectMapper.serialize;
import static com.appsflyer.donkey.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class RingErrorHandlerTest {
  
  private Server server;
  private static final RouteSupplier routeSupplier = new RingRouteSupplier();
  
  
  private static ServerConfig.ServerConfigBuilder getDefaultConfigBuilder(Vertx vertx) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(1)
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routeList(RouteList.from(routeSupplier.internalServerError()))
                       .routeCreatorFactory(TestUtil::newRouteCreator);
  }
  
  @AfterEach
  void tearDown() throws ServerShutdownException {
    server.shutdownSync();
    server = null;
  }
  
  @Test
  void testCustomInternalServerErrorHandler(Vertx vertx, VertxTestContext testContext) throws
                                                                                       ServerInitializationException {
    IFn handler = mock(IFn.class);
    when(handler.invoke(any())).thenAnswer(this::internalServerErrorHandler);
    
    ServerConfig config = getDefaultConfigBuilder(vertx)
        .errorHandler(RingErrorHandler.create().add(500, handler))
        .build();
    
    server = ServerImpl.create(config);
    server.startSync();
    var path = "/internal-server-error";
    doGet(vertx, path)
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert500(response);
          var body = (IPersistentMap) parseResponseBody(response);
          assertEquals(path, body.valAt("path"));
          assertEquals("Internal server error route", ((IPersistentMap) body.valAt("cause")).valAt("message"));
          testContext.completeNow();
        })));
  }
  
  /**
   * Returns a response map where the body is the argument of the handler
   * serialized as json.
   */
  @NotNull
  private Object internalServerErrorHandler(InvocationOnMock invocationOnMock) {
    return RT.map(Keyword.intern("status"), 500,
                  Keyword.intern("body"), serialize(invocationOnMock.getArgument(0)));
  }
  
  @Test
  void testCustomNotFoundHandler(Vertx vertx, VertxTestContext testContext) throws
                                                                            ServerInitializationException {
    IFn handler = mock(IFn.class);
    when(handler.invoke(any())).thenAnswer(this::notFoundHandler);
    
    ServerConfig config = getDefaultConfigBuilder(vertx)
        .errorHandler(RingErrorHandler.create().add(404, handler))
        .build();
    
    server = ServerImpl.create(config);
    server.startSync();
    var path = "/not-found";
    doGet(vertx, path)
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert404(response);
          var body = (IPersistentMap) parseResponseBody(response);
          assertEquals(path, body.valAt("path"));
          assertNull(body.valAt("cause"));
          testContext.completeNow();
        })));
  }
  
  /**
   * Returns a response map where the body is the argument of the handler
   * serialized as json.
   */
  @NotNull
  private Object notFoundHandler(InvocationOnMock invocationOnMock) {
    return RT.map(Keyword.intern("body"), serialize(invocationOnMock.getArgument(0)));
  }
}
