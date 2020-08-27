package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.TestUtil;
import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerConfigBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.appsflyer.donkey.TestUtil.*;
import static io.vertx.core.http.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class ContentTypeHandlerTest {
  
  @Test
  void testContentTypeNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) {
    var server = Server.create(TestUtil.getDefaultConfigBuilder().build());
    server.start();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          server.shutdown();
          assert200(response);
          assertNull(response.getHeader("content-type"));
          testContext.completeNow();
        })));
  }
  
  @Test
  void testAddingContentTypeHeader(Vertx vertx, VertxTestContext testContext) throws
                                                                              InterruptedException {
    var routes =
        List.of(
            Map.of("content-type", "text/plain",
                   "uri", "/plain-text",
                   "body", "Hello world"),
            
            Map.of("content-type", "text/html",
                   "uri", "/html",
                   "body", "<!DOCTYPE html><html><body>Hello world</body></html>"),
            
            Map.of("content-type", "application/json",
                   "uri", "/json",
                   "body", "{\"say\":\"Hello world\"}"),
            
            Map.of("content-type", "application/octet-stream",
                   "uri", "/octet-stream",
                   "body", "Hello World"));
    
    var routeDescriptors =
        routes.stream()
              .map(entry -> routeForContentType(entry.get("uri"), entry.get("content-type"))
                  .handler(ctx -> ctx.response().end(entry.get("body"))))
              .toArray(RouteDescriptor[]::new);
    
    ServerConfig config = getDefaultConfigBuilder(
        newRouterDefinitionWithContentType(routeDescriptors))
        .addContentTypeHeader(true)
        .build();
    
    var server = Server.create(config);
    
    server.start();
    WebClient client = WebClient.create(vertx);
    
    Checkpoint responsesReceived = testContext.checkpoint(routes.size());
    CountDownLatch shutdownServerLatch = new CountDownLatch(routes.size());
    
    routes.forEach(
        v -> client.request(GET, getDefaultAddress(), v.get("uri"))
                   .putHeader("Accept", v.get("content-type"))
                   .send(testContext.succeeding(response -> testContext.verify(() -> {
                     shutdownServerLatch.countDown();
                     assert200(response);
                     assertEquals(v.get("content-type"), response.getHeader("content-type"));
                     assertEquals(v.get("body"), response.bodyAsString());
                     responsesReceived.flag();
                   }))));
    
    shutdownServerLatch.await(5, TimeUnit.SECONDS);
    server.shutdown();
  }
  
  private RouteDescriptor routeForContentType(String uri, String contentType) {
    return RouteDescriptor.create().path(PathDescriptor.create(uri)).addProduces(contentType);
  }
  
  private RouterDefinition newRouterDefinitionWithContentType(RouteDescriptor... routeDescriptors) {
    return new RouterDefinition(List.of(routeDescriptors));
  }
  
  private ServerConfigBuilder getDefaultConfigBuilder(RouterDefinition routerDefinition) {
    return ServerConfig.builder()
                       .vertxOptions(new VertxOptions())
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routerDefinition(routerDefinition)
                       .routeCreatorSupplier(TestUtil::newRouteCreator);
  }
}
