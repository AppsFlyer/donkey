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

import com.appsflyer.donkey.TestUtil;
import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerConfig.ServerConfigBuilder;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.*;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ContentTypeHandlerTest {
  
  private Server server;
  
  private static RouteDefinition routeForContentType(String uri, String contentType) {
    return RouteDefinition.create().path(uri).addProduces(contentType);
  }
  
  private static ServerConfigBuilder getDefaultConfigBuilder(Vertx vertx, RouteList routeList) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(1)
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routeList(routeList)
                       .routeCreatorFactory(TestUtil::newRouteCreator);
  }
  
  @AfterEach
  void tearDown() throws ServerShutdownException {
    server.shutdownSync();
    server = null;
  }
  
  @Test
  void testContentTypeNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) throws
                                                                                      ServerInitializationException {
    server = Server.create(TestUtil.getDefaultConfigBuilder(vertx).build());
    server.startSync();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          assertNull(response.getHeader(CONTENT_TYPE.toString()));
          testContext.completeNow();
        })));
  }
  
  @Test
  void testAddingContentTypeHeader(Vertx vertx, VertxTestContext testContext) throws
                                                                              Exception {
    var routes =
        List.of(
            Map.of(CONTENT_TYPE, "text/plain",
                   "uri", "/plain-text",
                   "body", "Hello world"),
            
            Map.of(CONTENT_TYPE, "text/html",
                   "uri", "/html",
                   "body", "<!DOCTYPE html><html><body>Hello world</body></html>"),
            
            Map.of(CONTENT_TYPE, "application/json",
                   "uri", "/json",
                   "body", "{\"say\":\"Hello world\"}"),
            
            Map.of(CONTENT_TYPE, "application/octet-stream",
                   "uri", "/octet-stream",
                   "body", "Hello World"));
    
    var routeDefinitions =
        routes.stream()
              .map(entry -> routeForContentType(entry.get("uri"), entry.get(CONTENT_TYPE))
                  .handler(ctx -> ctx.response().end(entry.get("body"))))
              .toArray(RouteDefinition[]::new);
    
    ServerConfig config = getDefaultConfigBuilder(vertx, RouteList.from(routeDefinitions))
        .addContentTypeHeader(true)
        .build();
    
    server = Server.create(config);
    server.startSync();
    
    WebClient client = WebClient.create(vertx);
    
    Checkpoint responsesReceived = testContext.checkpoint(routes.size());
    CountDownLatch shutdownServerLatch = new CountDownLatch(routes.size());
    
    routes.forEach(
        v -> client.request(GET, getDefaultAddress(), v.get("uri"))
                   .putHeader("Accept", v.get(CONTENT_TYPE))
                   .send(testContext.succeeding(response -> testContext.verify(() -> {
                     shutdownServerLatch.countDown();
                     assert200(response);
                     assertEquals(v.get(CONTENT_TYPE), response.getHeader(CONTENT_TYPE.toString()));
                     assertEquals(v.get("body"), response.bodyAsString());
                     responsesReceived.flag();
                   }))));
    
    shutdownServerLatch.await(5, TimeUnit.SECONDS);
  }
  
}
