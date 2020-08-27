package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.route.AbstractRouteCreator;
import com.appsflyer.donkey.route.RouteCreator;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import com.appsflyer.donkey.server.Server;
import com.appsflyer.donkey.server.ServerConfig;
import com.appsflyer.donkey.server.ServerConfigBuilder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.appsflyer.donkey.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(VertxExtension.class)
class DateHeaderGeneratorTest {
  
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME;
  
  private ServerConfigBuilder getDefaultConfigBuilder() {
    return ServerConfig.builder()
                       .vertxOptions(new VertxOptions())
                       .serverOptions(new HttpServerOptions().setPort(DEFAULT_PORT))
                       .routerDefinition(defaultRouterDefinition())
                       .routeCreatorSupplier(this::newRouteCreator);
  }
  
  private RouterDefinition defaultRouterDefinition() {
    return new RouterDefinition(List.of(
        RouteDescriptor.create().handler(ctx -> ctx.response().end())));
  }
  
  private RouteCreator newRouteCreator(Router router, RouterDefinition routerDefinition) {
    return new AbstractRouteCreator(router, routerDefinition) {
      @Override
      protected void buildRoute(Route route, RouteDescriptor rd) {
        route.handler(rd.handler());
      }
    };
  }
  
  @Test
  void testDateHeaderNotIncludedByDefault(Vertx vertx, VertxTestContext testContext) {
    var server = Server.create(getDefaultConfigBuilder().build());
    server.start();
    
    doGet(vertx, "/")
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          server.shutdown();
          assert200(response);
          assertNull(response.getHeader("Date"));
          testContext.completeNow();
        })));
  }
  
  @Test
  void testAddingDateHeader(Vertx vertx, VertxTestContext testContext) {
    var server = Server.create(getDefaultConfigBuilder()
                                   .addDateHeader(true)
                                   .build());
    server.start();
  
    Checkpoint responsesReceived = testContext.checkpoint(2);
  
    AtomicReference<String> lastDate = new AtomicReference<>("");
    Handler<Long> testCase = v -> doGet(vertx, "/")
        .onComplete(ignore -> {
          //Second time we can shutdown the server
          if (!lastDate.get().isBlank()) {
            server.shutdown();
          }
        })
        .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
          assert200(response);
          // Assert that the Date header is a valid date, and that it changed
          //since the last request.
          String date = response.getHeader("Date");
          assertNotEquals(lastDate.getAndSet(date), date);
          assertValidDate(date);
          responsesReceived.flag();
        })));
  
    //Call one time now
    testCase.handle(0L);
  
    //Call again in one second
    vertx.setTimer(1000, testCase);
  }
  
  private void assertValidDate(CharSequence date) {
    assertDoesNotThrow(() -> DATE_FORMAT.parse(date));
  }
}
