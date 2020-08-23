package com.appsflyer.donkey.route.handler.ring;

import com.appsflyer.donkey.route.handler.AdapterFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

class RingAdapterFactoryTest {
  
  @Test
  void testCreatingRequestHandler() {
    AdapterFactory adapterFactory = new RingAdapterFactory();
    assertThat(adapterFactory.requestAdapter(), instanceOf(RingRequestAdapter.class));
  }
  
  @Test
  void testCreatingResponseHandler() {
    AdapterFactory adapterFactory = new RingAdapterFactory();
    assertThat(adapterFactory.responseAdapter(), instanceOf(RingResponseAdapter.class));
  }
}
