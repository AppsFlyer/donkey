/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.route;

import com.appsflyer.donkey.server.ring.route.RingRouteCreatorFactory;
import com.appsflyer.donkey.server.router.RouterFactory;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.*;
import static com.appsflyer.donkey.server.route.PathDefinition.MatchType.REGEX;
import static io.vertx.core.http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class IntegrationTest {
  
  private static final String dummyJson = "{\"foo\":\"bar\"}";
  
  private RouterFactory newRouterFactory(Vertx vertx, List<RouteDefinition> routes) {
    return RouterFactory.create(vertx, newHandlerConfig(routes));
  }
  
  private RouteList newHandlerConfig(List<RouteDefinition> routes) {
    return new RouteList(routes);
  }
  
  private void assertContextSuccess(VertxTestContext testContext) throws
                                                                  Throwable {
    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
  
  private RequestOptions optionsForUri(String uri) {
    return new RequestOptions()
        .setHost(getDefaultAddress().host())
        .setPort(DEFAULT_PORT)
        .setURI(uri);
  }
  
  private Router defineRoutes(Vertx vertx, Checkpoint requestsServed) {
    Handler<RoutingContext> handler = ctx -> {
      ctx.response().end(ctx.request().params().toString());
      requestsServed.flag();
    };
  
    var getFoo = RouteDefinition.create()
                                .addMethod(GET)
                                .path(PathDefinition.create("/foo"))
                                .handler(handler);
  
    var postFooBar = RouteDefinition.create()
                                    .addMethod(POST)
                                    .path(PathDefinition.create("/foo/bar"))
                                    .handler(handler);
  
    var postOrPutJson = RouteDefinition.create()
                                       .addMethod(POST)
                                       .addMethod(PUT)
                                       .path(PathDefinition.create("/json"))
                                       .addConsumes("application/json")
                                       .addProduces("application/json")
                                       .handler(handler);
  
    var getPathVariable = RouteDefinition.create()
                                         .addMethod(GET)
                                         .path(PathDefinition.create("/token/:tokenId"))
                                         .handler(handler);
  
    var getRegexPath = RouteDefinition.create()
                                      .addMethod(GET)
                                      .path(PathDefinition.create("/id/(\\d+)", REGEX))
                                      .handler(handler);
  
    var postComplexRegexPath = RouteDefinition.create()
                                              .addMethod(POST)
                                              .path(PathDefinition.create("/([a-z]+-company)/(\\d+)/(account.{3})-dept", REGEX))
                                              .handler(handler);
  
    return newRouterFactory(
        vertx, List.of(getFoo,
                       postFooBar,
                       postOrPutJson,
                       getPathVariable,
                       getRegexPath,
                       postComplexRegexPath))
        .withRouteCreator(new RingRouteCreatorFactory());
  }
  
  private Future<HttpServer> startServer(
      Vertx vertx, VertxTestContext testContext, Handler<HttpServerRequest> router) {
    Checkpoint serverStarted = testContext.checkpoint();
    Promise<HttpServer> promise = Promise.promise();
    vertx.createHttpServer()
         .requestHandler(router)
         .listen(DEFAULT_PORT, v -> {
           if (v.failed()) {
             testContext.failNow(v.cause());
           }
           serverStarted.flag();
           promise.complete(v.result());
         });
    return promise.future();
  }
  
  @Test
  void testRoutingByMethod(Vertx vertx, VertxTestContext testContext) throws
                                                                      Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          var client = WebClient.create(vertx);
  
  
          doGet(client, "/foo?fizz=buzz").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                responsesReceived.flag();
              })));
  
          doPost(client, "/foo").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert405(response);
                responsesReceived.flag();
              })));
        });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testRoutingByConsumeContentType(Vertx vertx, VertxTestContext testContext) throws
                                                                                  Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          
          var client = WebClient.create(vertx);
          
          client.request(POST, optionsForUri("/json"))
                .sendJson(dummyJson, testContext.succeeding(response -> testContext.verify(() -> {
                  assert200(response);
                  responsesReceived.flag();
                })));
          
          client.request(POST, getDefaultAddress(), "/json")
                .putHeader("content-type", "application/octet-stream")
                .send(testContext.succeeding(response -> testContext.verify(() -> {
                  assert415(response);
                  responsesReceived.flag();
                })));
        });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testRoutingByProduceContentType(Vertx vertx, VertxTestContext testContext) throws
                                                                                  Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          client.request(PUT, optionsForUri("/json"))
                .putHeader("Accept", "application/json")
                .sendJson(dummyJson, testContext.succeeding(response -> testContext.verify(() -> {
                  assert200(response);
                  responsesReceived.flag();
                })));
          
          client.request(PUT, optionsForUri("/json"))
                .putHeader("Accept", "text/html")
                .sendJson(dummyJson, testContext.succeeding(response -> testContext.verify(() -> {
                  assert406(response);
                  responsesReceived.flag();
                })));
        });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testRoutingWithPathVariable(Vertx vertx, VertxTestContext testContext) throws
                                                                              Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(1);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          var client = WebClient.create(vertx);
  
          doGet(client, "/token/fizzbuzz?foo=bar&bazz=fuzz").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals(
                    String.join(System.lineSeparator(), "foo: bar", "bazz: fuzz", "tokenId: fizzbuzz"),
                    response.bodyAsString().trim());
                responsesReceived.flag();
              })));
        });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testRoutingWithRegexPath(Vertx vertx, VertxTestContext testContext) throws
                                                                           Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          var client = WebClient.create(vertx);
  
          doGet(client, "/id/12345").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals("param0: 12345", response.bodyAsString().trim());
                responsesReceived.flag();
              })));
  
          doGet(client, "/id/not-a-number").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert404(response);
                responsesReceived.flag();
              })));
        });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testRoutingWithComplexRegexPath(Vertx vertx, VertxTestContext testContext) throws
                                                                                  Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(1);
    
    Router router = defineRoutes(vertx, requestsServed);
    
    startServer(vertx, testContext, router)
        .onComplete(v -> {
          var client = WebClient.create(vertx);
  
          doPost(client, "/xyz-company/321/accounting-dept").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals(
                    String.join(System.lineSeparator(),
                                "param0: xyz-company",
                                "param1: 321",
                                "param2: accounting"),
                    response.bodyAsString().trim());
                responsesReceived.flag();
              })));
  
          doPost(client, "/xyz-company/321/marketing-dept").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert404(response);
                responsesReceived.flag();
              })));
  
        });
    
    assertContextSuccess(testContext);
  }
}
