package com.appsflyer.donkey.route.handler.ring;

import com.appsflyer.donkey.route.handler.HandlerFactory;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RingHandlerFactoryTest
{
  
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
