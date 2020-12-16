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

package com.appsflyer.donkey.server.ring.handler;

import org.junit.jupiter.api.Test;
import com.appsflyer.donkey.server.handler.AdapterFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

class RingAdapterFactoryTest {
  
  @Test
  void testCreatingRequestHandler() {
    AdapterFactory adapterFactory = RingAdapterFactory.create();
    assertThat(adapterFactory.requestAdapter(), instanceOf(RingRequestAdapter.class));
  }
  
  @Test
  void testCreatingResponseHandler() {
    AdapterFactory adapterFactory = RingAdapterFactory.create();
    assertThat(adapterFactory.responseAdapter(), instanceOf(RingResponseAdapter.class));
  }
}
