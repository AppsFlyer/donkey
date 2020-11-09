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

package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import com.appsflyer.donkey.client.ClientConfig;
import com.appsflyer.donkey.server.ring.handler.RingRequestAdapter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.*;
import static com.appsflyer.donkey.client.ring.ClojureRequestField.*;
import static io.vertx.core.http.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.*;

@Tag("Integration")
@ExtendWith(VertxExtension.class)
class RingClientTest {
  
  private static final VertxRouteSupplier routeSupplier = new VertxRouteSupplier();
  private static final Keyword getMethod = Keyword.intern(GET.name().toLowerCase());
  private static final int SSL_PORT = 8443;
  private static Vertx vertx;
  private static RingClient client;
  
  @BeforeAll
  static void beforeAll(Vertx vertx) throws InterruptedException {
    RingClientTest.vertx = vertx;
    client = makeClient();
    var latch = new CountDownLatch(2);
    startServer(makeRouter(), DEFAULT_PORT).onComplete(v -> latch.countDown());
    startServer(makeRouter(), SSL_PORT).onComplete(v -> latch.countDown());
    latch.await(5, TimeUnit.SECONDS);
  }
  
  @AfterAll
  static void afterAll() {
    vertx.close();
  }
  
  static Future<Void> startServer(Handler<HttpServerRequest> router, int port) {
    Promise<Void> promise = Promise.promise();
    HttpServerOptions options = new HttpServerOptions()
        .setCompressionSupported(true)
        .setDecompressionSupported(true)
        .setPort(port);
    
    if (port == SSL_PORT) {
      options.setSsl(true)
             .setSni(true)
             .setKeyStoreOptions(
                 new JksOptions()
                     .setPath("server-keystore.jks")
                     .setPassword("wibble"));
    }
    
    vertx.createHttpServer(options)
         .requestHandler(router)
         .listen(res -> {
           if (res.failed()) {
             promise.fail(res.cause());
           } else {
             promise.complete();
           }
         });
    return promise.future();
  }
  
  private static Router makeRouter() {
    var router = Router.router(vertx);
    router.route().handler(new RingRequestAdapter());
    routeSupplier.root200(router);
    routeSupplier.echo(router);
    routeSupplier.timeout(router);
    
    return router;
  }
  
  static RingClient makeClient() {
    return RingClient.create(
        ClientConfig.builder()
                    .clientOptions(new WebClientOptions()
                                       .setTryUseCompression(true)
                                       .setTrustAll(true))
                    .vertx(vertx)
                    .build());
  }
  
  @Test
  void testHttpMethodRequired() {
    assertThrows(NullPointerException.class, () -> client.request(null));
    
    Throwable ex = assertThrows(NullPointerException.class, () -> client.request(RT.map()));
    assertTrue(ex.getMessage().contains("method is missing"));
    
    ex = assertThrows(NullPointerException.class, () -> client.request(RT.map(METHOD.keyword(), null)));
    assertTrue(ex.getMessage().contains("method is missing"));
    
    assertThrows(ClassCastException.class, () -> client.request(RT.map(METHOD.keyword(), "foo")));
  }
  
  @Test
  void testPort(VertxTestContext testContext) throws Throwable {
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               PORT.keyword(), DEFAULT_PORT));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          responsesReceived.flag();
        })));
    
    request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               PORT.keyword(), 8081));
    
    client.send(request).onComplete(testContext.failing(
        ex -> testContext.verify(() -> {
          assertTrue(ex.getMessage().contains("Connection refused"));
          responsesReceived.flag();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testHost(VertxTestContext testContext) throws Throwable {
    Checkpoint responsesReceived = testContext.checkpoint(2);
    
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               PORT.keyword(), DEFAULT_PORT,
               HOST.keyword(), "localhost"));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          responsesReceived.flag();
        })));
    
    request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               PORT.keyword(), DEFAULT_PORT,
               HOST.keyword(), "foo.bar"));
    
    client.send(request).onComplete(testContext.failing(
        ex -> testContext.verify(() -> {
          assertTrue(ex.getMessage().contains("failed to resolve"));
          responsesReceived.flag();
        })));
    
    assertContextSuccess(testContext, 10, TimeUnit.SECONDS);
  }
  
  @Test
  void testSsl(VertxTestContext testContext) throws Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), SSL_PORT,
               SSL.keyword(), true));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          assertEquals(SSL_PORT, ringRequest.valAt("server-port"));
          assertEquals("https", ringRequest.valAt("scheme"));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testQueryParams(VertxTestContext testContext) throws
                                                     Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), DEFAULT_PORT,
               QUERY_PARAMS.keyword(), RT.map("foo", "bar", "fizz", "baz")));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          assertEquals("foo=bar&fizz=baz", ringRequest.valAt("query-string"));
          
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testHeaders(VertxTestContext testContext) throws
                                                 Throwable {
    Map<String, String> headers = Map.of(
        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
        "Accept-Encoding", "gzip, deflate, br",
        "Accept-Language", "en-US,en;q=0.9,he;q=0.8",
        "Cache-Control", "max-age=0",
        "Connection", "keep-alive",
        "Cookie", "_octo=GH1.1.956261828.1568396491; _device_id=c4b7e18bd12b94e02574324db958df2a; user_session=yB5jlrFVEQ4B41-CTybGsWbbew2VFVAJygNznBsrc0B_yuVd; __Host-user_session_same_site=yB5jlrFVEQ4B41-CTybGsWbbew2VFVAJygNznBsrc0B_yuVd; logged_in=yes; dotcom_user=yaronel; tz=Asia%2FJerusalem; _gh_sess=t05fUjSAe%2Bwrhsv%2FPgFg7zc%2B5d0ZKQHpwxq%2FNwyrxIOY0JrzDQ1yCBXJaJe4aJvErQW326uDpYl29mlyed30nVnJeiYDLYAGjuiy7%2Br%2FT9ZX6Vu8zt1LEFFgF5a0iLPkYPDipj0bmM09YvnM%2FEmv%2BZgZ3eUH6lXMr%2FEUDG10dDkUB%2Fk4i8ipTBr3SCUTiVxfs%2BE%2FNVOEZ3duG0ni05ozRBZ2pyBmD3MlhfUBeZLSxzxzpZDQZSkOX%2FFmItMuIDImKUKu91bUESk%2BiDrmjRI3kJbxHNRA2mSoMl27Awb05AaucT2miOixybgwNz9Lu%2Bz9%2BTf37p6ND6Z5nEghtKdpwW1ZP5Nli0kz%2BvDXEoyAuBBxc%2B4xDPNBryyn1KTHpjj0dF7xogyBp7IdIMbxjgtwYAEY%2F83jSLI%2BywXQgc9hCd2t5Y%2BUFYCoAzO2PJw%3D--NDAZ6dNgZV9Xqsq%2F--wBb1qDkI7qcIlUXPu2Ev4w%3D%3D",
        "DNT", "1",
        "Referer", "https://example.com/donkey",
        "Sec-Fetch-Site", "same-origin",
        "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
    
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), DEFAULT_PORT,
               HEADERS.keyword(), PersistentHashMap.create(headers)));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          IPersistentMap ringRequestHeaders = (IPersistentMap) ringRequest.valAt("headers");
          headers.forEach((k, v) -> assertEquals(v, ringRequestHeaders.valAt(k.toLowerCase())));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testBasicAuth(VertxTestContext testContext) throws
                                                   Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), DEFAULT_PORT,
               BASIC_AUTH.keyword(), RT.map("id", "foo", "password", "bar")));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          String expectedAuthHeader = "Basic " + Base64.getEncoder()
                                                       .encodeToString(
                                                           "foo:bar".getBytes());
          assertEquals(expectedAuthHeader,
                       ((IPersistentMap) ringRequest.valAt("headers"))
                           .valAt("authorization"));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testBearerTokenAuth(VertxTestContext testContext) throws
                                                         Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), DEFAULT_PORT,
               BEARER_TOKEN.keyword(), "my-access-token"));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          assertEquals("Bearer my-access-token",
                       ((IPersistentMap) ringRequest.valAt("headers"))
                           .valAt("authorization"));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testTimeout(VertxTestContext testContext) throws
                                                 Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/timeout",
               PORT.keyword(), DEFAULT_PORT,
               TIMEOUT.keyword(), 1));
    
    client.send(request).onComplete(testContext.failing(
        ex -> testContext.verify(() -> {
          assertTrue(ex.getMessage().contains("timeout period of 1000ms"));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @SuppressWarnings("JUnitTestMethodWithNoAssertions")
  @Test
  void testAbsoluteUrl(VertxTestContext testContext) throws
                                                     Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URL.keyword(), "http://localhost:" + DEFAULT_PORT));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testAbsoluteHttpsUrl(VertxTestContext testContext) throws
                                                          Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URL.keyword(), "https://localhost:" + SSL_PORT + "/echo",
               SSL.keyword(), true));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          assertEquals(SSL_PORT, ringRequest.valAt("server-port"));
          assertEquals("https", ringRequest.valAt("scheme"));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testAbsoluteUrlQueryParams(VertxTestContext testContext) throws
                                                                Throwable {
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URL.keyword(), "http://localhost:" + DEFAULT_PORT + "/echo?foo=bar",
               QUERY_PARAMS.keyword(), RT.map("fizz", "baz")));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          assertEquals("foo=bar&fizz=baz", ringRequest.valAt("query-string"));
          
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testAbsoluteUrlHeaders(VertxTestContext testContext) throws
                                                            Throwable {
    Map<String, String> headers = Map.of(
        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
        "Accept-Encoding", "gzip, deflate, br",
        "Accept-Language", "en-US,en;q=0.9,he;q=0.8",
        "Cache-Control", "max-age=0",
        "Connection", "keep-alive",
        "Cookie", "_octo=GH1.1.956261828.1568396491; _device_id=c4b7e18bd12b94e02574324db958df2a; user_session=yB5jlrFVEQ4B41-CTybGsWbbew2VFVAJygNznBsrc0B_yuVd; __Host-user_session_same_site=yB5jlrFVEQ4B41-CTybGsWbbew2VFVAJygNznBsrc0B_yuVd; logged_in=yes; dotcom_user=yaronel; tz=Asia%2FJerusalem; _gh_sess=t05fUjSAe%2Bwrhsv%2FPgFg7zc%2B5d0ZKQHpwxq%2FNwyrxIOY0JrzDQ1yCBXJaJe4aJvErQW326uDpYl29mlyed30nVnJeiYDLYAGjuiy7%2Br%2FT9ZX6Vu8zt1LEFFgF5a0iLPkYPDipj0bmM09YvnM%2FEmv%2BZgZ3eUH6lXMr%2FEUDG10dDkUB%2Fk4i8ipTBr3SCUTiVxfs%2BE%2FNVOEZ3duG0ni05ozRBZ2pyBmD3MlhfUBeZLSxzxzpZDQZSkOX%2FFmItMuIDImKUKu91bUESk%2BiDrmjRI3kJbxHNRA2mSoMl27Awb05AaucT2miOixybgwNz9Lu%2Bz9%2BTf37p6ND6Z5nEghtKdpwW1ZP5Nli0kz%2BvDXEoyAuBBxc%2B4xDPNBryyn1KTHpjj0dF7xogyBp7IdIMbxjgtwYAEY%2F83jSLI%2BywXQgc9hCd2t5Y%2BUFYCoAzO2PJw%3D--NDAZ6dNgZV9Xqsq%2F--wBb1qDkI7qcIlUXPu2Ev4w%3D%3D",
        "DNT", "1",
        "Referer", "https://example.com/donkey",
        "Sec-Fetch-Site", "same-origin",
        "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
    
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URL.keyword(), "http://localhost:" + DEFAULT_PORT + "/echo",
               HEADERS.keyword(), PersistentHashMap.create(headers)));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap ringRequest = (IPersistentMap) parseResponseBody(response);
          IPersistentMap ringRequestHeaders = (IPersistentMap) ringRequest.valAt("headers");
          headers.forEach((k, v) -> assertEquals(v, ringRequestHeaders.valAt(k.toLowerCase())));
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
}
