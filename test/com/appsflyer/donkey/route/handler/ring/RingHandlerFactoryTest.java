package com.appsflyer.donkey.route.handler.ring;

import com.appsflyer.donkey.route.HandlerMode;
import com.appsflyer.donkey.route.RouteDescriptor;
import com.appsflyer.donkey.route.handler.HandlerFactory;
import com.appsflyer.donkey.route.ring.RingRouteDescriptor;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RingHandlerFactoryTest
{
  @Test
  void testCreatingUserHandler()
  {
    HandlerFactory handlerFactory = new RingHandlerFactory();
    RouteDescriptor routeDescriptor = new RingRouteDescriptor().handler(ctx -> null);
    
    Handler<RoutingContext> routeHandler = handlerFactory.handlerFor(
        routeDescriptor.handlerMode(HandlerMode.BLOCKING));
    
    assertThat(routeHandler, instanceOf(BlockingRingHandler.class));
  
    routeHandler = handlerFactory.handlerFor(
        routeDescriptor.handlerMode(HandlerMode.NON_BLOCKING));
    
    assertThat(routeHandler, instanceOf(RingHandler.class));
    
    assertThrows(NullPointerException.class, () -> handlerFactory.handlerFor(null));
  }
  
  @Test
  void testCreatingRequestHandler()
  {
    HandlerFactory handlerFactory = new RingHandlerFactory();
    assertThat(handlerFactory.requestHandler(), instanceOf(RingRequestHandler.class));
  }
  
  @Test
  void testCreatingResponseHandler()
  {
    HandlerFactory handlerFactory = new RingHandlerFactory();
    assertThat(handlerFactory.responseHandler(Vertx.vertx()), instanceOf(RingResponseHandler.class));
    
    assertThrows(NullPointerException.class, () -> handlerFactory.responseHandler(null));
  }
}
