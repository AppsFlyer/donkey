package com.appsflyer.donkey.server.ring.handler;

import org.junit.jupiter.api.Test;
import com.appsflyer.donkey.server.handler.AdapterFactory;
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
