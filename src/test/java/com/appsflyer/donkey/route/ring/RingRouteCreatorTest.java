package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.RouterDefinition;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.appsflyer.donkey.route.PathDescriptor.MatchType.REGEX;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class RingRouteCreatorTest {
  
  @Test
  void testBuildRoute(Vertx vertx, VertxTestContext testContext) {
    var routeDescriptor =
        RouteDescriptor.create()
                       .addMethod(HttpMethod.GET)
                       .addMethod(HttpMethod.POST)
                       .path(PathDescriptor.create("/foo"))
                       .handler(RoutingContext::next);
    
    var routeCreator = new RingRouteCreator(Router.router(vertx), RouterDefinition.from(routeDescriptor));
    Router router = routeCreator.addRoutes();
    assertEquals(1, router.getRoutes().size());
    Route route = router.getRoutes().get(0);
    
    assertEquals(routeDescriptor.path().value(), route.getPath());
    assertEquals(routeDescriptor.methods(), route.methods());
    assertFalse(route.isRegexPath());
    
    testContext.completeNow();
  }
  
  @Test
  void testBuildRegexRoute(Vertx vertx, VertxTestContext testContext) {
    var routeDescriptor =
        RouteDescriptor.create()
                       .path(PathDescriptor.create("/foo/[0-9]+", REGEX))
                       .handler(RoutingContext::next);
    
    var routeCreator = new RingRouteCreator(Router.router(vertx), RouterDefinition.from(routeDescriptor));
    Router router = routeCreator.addRoutes();
    assertEquals(1, router.getRoutes().size());
    Route route = router.getRoutes().get(0);
    assertTrue(route.isRegexPath());
    
    testContext.completeNow();
  }
}
