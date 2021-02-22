/*
 * Copyright 2020-2021 AppsFlyer
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
import com.appsflyer.donkey.server.ServerImpl;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import com.appsflyer.donkey.server.route.PathDefinition;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.appsflyer.donkey.TestUtil.*;
import static io.vertx.core.http.HttpMethod.*;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class StaticResourcesHandlerTest {
  
  private static Server server;
  private static WebClient client;
  
  @BeforeAll
  static void beforeAll(Vertx vertx) throws ServerInitializationException {
    server = ServerImpl.create(getConfigBuilder(vertx).build());
    client = WebClient.create(vertx);
    server.startSync();
  }
  
  @AfterAll
  static void tearDown() throws ServerShutdownException {
    client.close();
    server.shutdownSync();
  }
  
  public static ServerConfigBuilder getConfigBuilder(Vertx vertx) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(2)
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routeList(newRouteList())
                       .routeCreatorFactory(TestUtil::newRouteCreator);
  }
  
  private static RouteList newRouteList() {
    StaticResourcesHandler handler = newStaticResourcesHandler();
    return RouteList.from(Stream.of(
        PathDefinition.create("/", PathDefinition.MatchType.REGEX),
        PathDefinition.create("/.+\\.json", PathDefinition.MatchType.REGEX),
        PathDefinition.create("/.+\\.gif", PathDefinition.MatchType.REGEX))
                                .map(p -> RouteDefinition.create().path(p).handler(handler))
                                .collect(Collectors.toList()));
    
  }
  
  private static StaticResourcesHandler newStaticResourcesHandler() {
    return StaticResourcesHandler.create(newStaticResourcesConfig());
  }
  
  private static StaticResourcesConfig newStaticResourcesConfig() {
    return new StaticResourcesConfig.Builder()
        .resourcesRoot("public")
        .enableCaching(true)
        .localCacheDuration(Duration.ofSeconds(60))
        .localCacheSize(10)
        .maxAge(Duration.ofSeconds(60))
        .indexPage("home.html")
        .build();
  }
  
  @Test
  void testFileNotFound(VertxTestContext testContext) {
    makeRequest(client, GET, "/foo.txt")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert404(response);
          testContext.completeNow();
        })));
  }
  
  @Test
  void testServesJSONFile(VertxTestContext testContext) {
    makeRequest(client, GET, "/hello.json")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          assertEquals(response.bodyAsJsonObject().getString("hello"), "world");
          testContext.completeNow();
        })));
  }
  
  @Test
  void testServesOnlyGetAndHeadRequests(VertxTestContext testContext) {
    makeRequest(client, HEAD, "/hello.json")
        .compose(response -> {
          assert200(response);
          return makeRequest(client, POST, "/hello.json");
        })
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert404(response);
          testContext.completeNow();
        })));
  }
  
  @Test
  void testResourceCaching(VertxTestContext testContext) {
    makeRequest(client, GET, "/transparent.gif")
        .compose(response -> {
          assert200(response);
          assertEquals(response.getHeader("content-type"), "image/gif");
          return makeRequest(client, GET, "/transparent.gif");
        })
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          assertNotNull(response.getHeader("last-modified"));
          Optional<String> maxAge =
              stream(response.getHeader("cache-control")
                             .split(","))
                  .map(String::trim)
                  .filter("max-age=60"::equals)
                  .findFirst();
          
          assertTrue(maxAge.isPresent());
          testContext.completeNow();
        })));
  }
}
