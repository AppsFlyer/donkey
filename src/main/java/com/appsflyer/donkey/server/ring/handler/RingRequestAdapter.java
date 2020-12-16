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

import io.vertx.ext.web.RoutingContext;

import static com.appsflyer.donkey.util.TypeConverter.toPersistentMap;

/**
 * Handler responsible for converting an {@link io.vertx.core.http.HttpServerRequest}
 * to a Ring compliant Clojure map.
 * <p></p>
 * See the Ring <a href="https://github.com/ring-clojure/ring/blob/master/SPEC">specification</a> for more details.
 */
public final class RingRequestAdapter implements RingHandler {
  
  public static RingHandler create() {
    return new RingRequestAdapter();
  }
  
  private RingRequestAdapter() {}
  
  @Override
  public void handle(RoutingContext ctx) {
    RingRequestField[] fields = RingRequestField.values();
    var values = new Object[fields.length * 2];
    
    var valueIndex = 0;
    for (int i = 0; i < fields.length; i++) {
      var field = fields[i];
      Object v = field.from(ctx);
      if (v != null) {
        values[valueIndex] = field.keyword();
        values[valueIndex + 1] = v;
        valueIndex += 2;
      }
    }
    
    if (valueIndex == values.length) {
      ctx.put(RING_HANDLER_RESULT, toPersistentMap(values));
    } else {
      var copy = new Object[valueIndex];
      System.arraycopy(values, 0, copy, 0, valueIndex);
      ctx.put(RING_HANDLER_RESULT, toPersistentMap(copy));
    }
    
    ctx.next();
  }
}
