package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.TestUtil;
import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerConfigBuilder;
import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
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
    
    var routeDescriptors =
        routes.stream()
              .map(entry -> routeForContentType(entry.get("uri"), entry.get(CONTENT_TYPE))
                  .handler(ctx -> ctx.response().end(entry.get("body"))))
              .toArray(RouteDescriptor[]::new);
    
    ServerConfig config = getDefaultConfigBuilder(
        vertx, newRouterDefinitionWithContentType(routeDescriptors))
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
  
  private RouteDescriptor routeForContentType(String uri, String contentType) {
    return RouteDescriptor.create().path(PathDescriptor.create(uri)).addProduces(contentType);
  }
  
  private RouterDefinition newRouterDefinitionWithContentType(RouteDescriptor... routeDescriptors) {
    return RouterDefinition.from(routeDescriptors);
  }
  
  private ServerConfigBuilder getDefaultConfigBuilder(Vertx vertx, RouterDefinition routerDefinition) {
    return ServerConfig.builder()
                       .vertx(vertx)
                       .instances(1)
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routerDefinition(routerDefinition)
                       .routeCreatorSupplier(TestUtil::newRouteCreator);
  }
}
