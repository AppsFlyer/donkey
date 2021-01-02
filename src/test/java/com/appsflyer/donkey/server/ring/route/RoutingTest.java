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

package com.appsflyer.donkey.server.ring.route;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.route.AbstractRoutingTest;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteList;
import com.appsflyer.donkey.server.route.RouteSupplier;
import io.netty.handler.codec.http.HttpVersion;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static com.appsflyer.donkey.TestUtil.*;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_OCTET_STREAM;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@ExtendWith(VertxExtension.class)
public class RoutingTest extends AbstractRoutingTest {
  
  private static final RouteSupplier routeSupplier = new RingRouteSupplier();
  
  @Override
  protected ServerConfig newServerConfig(Vertx vertx, RouteList routeList) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(4)
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routeCreatorFactory(RingRouteCreatorFactory.create())
                       .routeList(routeList)
                       .build();
  }
  
  @Override
  protected RouteSupplier routeSupplier() {
    return routeSupplier;
  }
  
  @Test
  void testRingCompliantRequest(Vertx vertx, VertxTestContext testContext) throws
                                                                           Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.echo(requestsServed);
    startServer(vertx, RouteList.from(route))
        .onComplete(v -> doGet(vertx, route.path().value() + "?foo=bar")
            .onComplete(testContext.succeeding(
                response -> testContext.verify(() -> {
                  assert200(response);
                  
                  var request = (IPersistentMap) parseResponseBody(response);
                  assertEquals(DEFAULT_PORT, request.valAt("server-port"));
                  assertEquals("localhost", request.valAt("server-name"));
                  assertThat((String) request.valAt("remote-addr"), startsWith("127.0.0.1:"));
                  assertEquals(route.path().value(), request.valAt("uri"));
                  assertEquals("foo=bar", request.valAt("query-string"));
                  assertEquals("http", request.valAt("scheme"));
                  assertEquals(HttpVersion.HTTP_1_1.text(), request.valAt("protocol"));
                  var headers = (IPersistentMap) request.valAt("headers");
                  assertEquals(2, headers.count());
                  assertEquals("localhost", headers.valAt("host"));
                  assertThat((String) headers.valAt("user-agent"), startsWith("Vert.x"));
                }))));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testHeaderNamesShouldBeLowercase(Vertx vertx, VertxTestContext testContext) throws
                                                                                   Throwable {
    var headers = Map.of(
        "Content-Type", "text/html",
        "Connection", "Keep-Alive",
        "KEEP-ALIVE", "30",
        "Cache-Control", "max-age=1",
        "If-Modified-Since", "Wed, 21 Oct 2015 07:28:00 GMT",
        "accept", "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
    
    Checkpoint requestsServed = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.echo(requestsServed);
    startServer(vertx, RouteList.from(route)).onComplete(id -> {
      var client = WebClient.create(vertx);
      
      HttpRequest<Buffer> request = client.request(GET, getRequestOptions(route.path().value()));
      headers.forEach(request::putHeader);
      request.send(testContext.succeeding(
          response -> testContext.verify(() -> {
            assert200(response);
            var ringRequestHeaders =
                (IPersistentMap) ((IPersistentMap) parseResponseBody(response)).valAt("headers");
            headers.forEach((k, v) -> assertEquals(v, ringRequestHeaders.valAt(k.toLowerCase())));
          })));
    });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testPostMultipartForm(Vertx vertx, VertxTestContext testContext) throws
                                                                        Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.postFormOrFile(requestsServed);
    startServer(vertx, RouteList.from(route)).onComplete(v -> {
      var client = WebClient.create(vertx);
    
      client.request(POST, getRequestOptions(route.path().value()))
            .sendMultipartForm(
                MultipartForm.create().attribute("foo", "bar"),
                testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert200(response);
                      var request = (IPersistentMap) parseResponseBody(response);
                      assertNotNull(request.valAt("form-params"));
                      assertEquals("bar",
                                   ((IPersistentMap) request.valAt("form-params"))
                                       .valAt("foo"));
                      responsesReceived.flag();
                    })));
    });
  
    assertContextSuccess(testContext);
  }
  
  @Test
  void testPostForm(Vertx vertx, VertxTestContext testContext) throws
                                                               Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.postFormOrFile(requestsServed);
    startServer(vertx, RouteList.from(route)).onComplete(v -> {
      var client = WebClient.create(vertx);
      
      client.request(POST, getRequestOptions(route.path().value()))
            .sendForm(
                MultiMap.caseInsensitiveMultiMap().add("foo", "bar"),
                testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert200(response);
                      var request = (IPersistentMap) parseResponseBody(response);
                      assertNotNull(request.valAt("form-params"));
                      assertEquals("bar",
                                   ((IPersistentMap) request.valAt("form-params"))
                                       .valAt("foo"));
                      responsesReceived.flag();
                    })));
    });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testPostOctetStream(Vertx vertx, VertxTestContext testContext) throws
                                                                      Throwable {
    Checkpoint requestsServed = testContext.checkpoint(1);
    Checkpoint responsesReceived = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.postFormOrFile(requestsServed);
    startServer(vertx, RouteList.from(route)).onComplete(v -> {
      var client = WebClient.create(vertx);
      
      client.request(POST, getRequestOptions(route.path().value()))
            .putHeader(CONTENT_TYPE.toString(), APPLICATION_OCTET_STREAM.toString())
            .sendBuffer(
                Buffer.buffer("foo bar"),
                testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert200(response);
                      var request = (IPersistentMap) parseResponseBody(response);
                      assertNotNull(request.valAt("body"));
                      assertEquals(Base64.getEncoder().encodeToString("foo bar".getBytes(StandardCharsets.UTF_8)),
                                   request.valAt("body"));
                      responsesReceived.flag();
                    })));
    });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testPostUnsupportedMediaType(Vertx vertx, VertxTestContext testContext) throws
                                                                               Throwable {
    Checkpoint responsesReceived = testContext.checkpoint(1);
    RouteDefinition route = routeSupplier.postFormOrFile(null);
    startServer(vertx, RouteList.from(route)).onComplete(v -> {
      var client = WebClient.create(vertx);
      
      client.request(POST, getRequestOptions(route.path().value()))
            .sendJson(
                "{\"foo\":\"bar\"}",
                testContext.succeeding(
                    response -> testContext.verify(() -> {
                      assert415(response);
                      responsesReceived.flag();
                    })));
    });
    
    assertContextSuccess(testContext);
  }
}
