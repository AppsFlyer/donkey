package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
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
  private static Vertx vertx;
  
  @BeforeAll
  static void beforeAll(Vertx vertx) throws InterruptedException {
    RingClientTest.vertx = vertx;
    var latch = new CountDownLatch(1);
    startServer(makeRouter()).onComplete(v -> latch.countDown());
    latch.await(5, TimeUnit.SECONDS);
  }
  
  @AfterAll
  static void afterAll() {
    vertx.close();
  }
  
  static Future<Void> startServer(Handler<HttpServerRequest> router) {
    return startServer(router, DEFAULT_PORT);
  }
  
  static Future<Void> startServer(Handler<HttpServerRequest> router, int port) {
    Promise<Void> promise = Promise.promise();
    HttpServerOptions options = new HttpServerOptions()
        .setCompressionSupported(true)
        .setDecompressionSupported(true)
        .setPort(port);
    
    if (port == 443) {
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
    
    return router;
  }
  
  private RingClient makeClient() {
    return RingClient.create(
        ClientConfig.builder()
                    .clientOptions(new WebClientOptions().setTrustAll(true))
                    .vertx(vertx)
                    .build());
  }
  
  @Test
  void testHttpMethodRequired() {
    RingClient client = makeClient();
    
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
    RingClient client = makeClient();
    
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
    RingClient client = makeClient();
    
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
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testSsl(VertxTestContext testContext) throws Throwable {
    RingClient client = makeClient();
    int port = 443;
    
    startServer(makeRouter(), port).onComplete(v -> {
      HttpRequest<Buffer> request = client.request(
          RT.map(METHOD.keyword(), getMethod,
                 URI.keyword(), "/echo",
                 PORT.keyword(), port,
                 SSL.keyword(), true));
      
      client.send(request).onComplete(testContext.succeeding(
          response -> testContext.verify(() -> {
            assert200(response);
            IPersistentMap responseMap = (IPersistentMap) parseResponseBody(response);
            assertEquals(port, responseMap.valAt("server-port"));
            assertEquals("https", responseMap.valAt("scheme"));
            testContext.completeNow();
          })));
    });
    
    assertContextSuccess(testContext);
  }
  
  @Test
  void testQueryParams(VertxTestContext testContext) throws
                                                     Throwable {
    RingClient client = makeClient();
    
    HttpRequest<Buffer> request = client.request(
        RT.map(METHOD.keyword(), getMethod,
               URI.keyword(), "/echo",
               PORT.keyword(), DEFAULT_PORT,
               QUERY_PARAMS.keyword(), RT.map("foo", "bar", "fizz", "baz")));
    
    client.send(request).onComplete(testContext.succeeding(
        response -> testContext.verify(() -> {
          assert200(response);
          IPersistentMap responseMap = (IPersistentMap) parseResponseBody(response);
          assertEquals("foo=bar&fizz=baz", responseMap.valAt("query-string"));
          
          testContext.completeNow();
        })));
    
    assertContextSuccess(testContext);
  }
}
