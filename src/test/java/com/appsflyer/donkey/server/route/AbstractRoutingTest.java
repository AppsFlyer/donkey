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

package com.appsflyer.donkey.server.route;

import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerImpl;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.*;
import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.core.http.HttpMethod.PUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractRoutingTest {
  
  private static final String dummyJson = "{\"foo\":\"bar\"}";
  private Server server;
  
  protected abstract ServerConfig newServerConfig(Vertx vertx, RouteList routeList);
  
  protected abstract RouteSupplier routeSupplier();
  
  protected Future<String> startServer(Vertx vertx, RouteList routeList) {
    server = ServerImpl.create(newServerConfig(vertx, routeList));
    return server.start();
  }
  
  protected Server server() {
    return server;
  }
  
  @AfterEach
  void tearDown() throws InterruptedException {
    if (server() != null) {
      var latch = new CountDownLatch(1);
      server().vertx().close(v -> latch.countDown());
      latch.await(2, TimeUnit.SECONDS);
    }
  }
  
  @Test
  void testRoutingByMethod(Vertx vertx, VertxTestContext testContext) throws
                                                                      Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(2);
    RouteDefinition route = routeSupplier().helloWorld(requestsServed);
    
    startServer(vertx, RouteList.from(route))
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          doGet(client, route.path().value()).onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                responsesReceived.flag();
              })));
          
          doPost(client, route.path().value()).onComplete(
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
    RouteDefinition route = routeSupplier().postOrPutJson(requestsServed, dummyJson);
    
    startServer(vertx, RouteList.from(route))
        .onComplete(v -> {
          
          var client = WebClient.create(vertx);
          
          client.request(POST, getRequestOptions(route.path().value()))
                .sendJson(dummyJson, testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert200(response);
                      responsesReceived.flag();
                    })));
          
          client.request(POST, getRequestOptions(route.path().value()))
                .putHeader("content-type", "application/octet-stream")
                .send(testContext.succeeding(
                    response -> testContext.verify(() -> {
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
    RouteDefinition route = routeSupplier().postOrPutJson(requestsServed, dummyJson);
    
    
    startServer(vertx, RouteList.from(route))
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          client.request(PUT, getRequestOptions(route.path().value()))
                .putHeader("Accept", "application/json")
                .sendJson(dummyJson, testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert200(response);
                      responsesReceived.flag();
                    })));
          
          client.request(PUT, getRequestOptions(route.path().value()))
                .putHeader("Accept", "text/html")
                .sendJson(dummyJson, testContext.succeeding(
                    response -> testContext.verify(() -> {
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
    String uri = "/token/:tokenId";
    
    startServer(vertx, RouteList.from(routeSupplier().getPathVariable(requestsServed, uri)))
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          doGet(client, "/token/fizzbuzz").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals("{\"tokenId\":\"fizzbuzz\"}", response.bodyAsString().trim());
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
    String uri = "/id/(\\d+)";
    
    startServer(vertx, RouteList.from(routeSupplier().getRegexPath(requestsServed, uri)))
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          doGet(client, "/id/12345").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals("{\"param0\":\"12345\"}", response.bodyAsString().trim());
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
    Checkpoint responsesReceived = testContext.checkpoint(2);
    String uri = "/([a-z]+-company)/(\\d+)/(account.{3})-dept";
    
    startServer(vertx, RouteList.from(routeSupplier().getRegexPath(requestsServed, uri)))
        .onComplete(v -> {
          var client = WebClient.create(vertx);
          
          doGet(client, "/xyz-company/321/accounting-dept").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert200(response);
                assertEquals(
                    "{\"param0\":\"xyz-company\",\"param1\":\"321\",\"param2\":\"accounting\"}",
                    response.bodyAsString().trim());
                responsesReceived.flag();
              })));
          
          doGet(client, "/xyz-company/321/marketing-dept").onComplete(
              testContext.succeeding(response -> testContext.verify(() -> {
                assert404(response);
                responsesReceived.flag();
              })));
        });
    
    assertContextSuccess(testContext);
  }
}
