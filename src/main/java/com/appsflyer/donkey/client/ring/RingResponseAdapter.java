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

package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

import static com.appsflyer.donkey.util.TypeConverter.toPersistentMap;

/**
 * Handler responsible for converting an {@link HttpResponse}
 * to a Ring compliant Clojure map.
 * <p></p>
 * See the Ring <a href="https://github.com/ring-clojure/ring/blob/master/SPEC">specification</a> for more details.
 */
public final class RingResponseAdapter implements Handler<AsyncResult<HttpResponse<Buffer>>> {
  
  public static RingResponseAdapter create(Promise<IPersistentMap> promise) {
    return new RingResponseAdapter(promise);
  }
  
  private final Promise<IPersistentMap> promise;
  
  private RingResponseAdapter(Promise<IPersistentMap> promise) {
    this.promise = promise;
  }
  
  @Override
  public void handle(AsyncResult<HttpResponse<Buffer>> event) {
    if (event.succeeded()) {
      HttpResponse<Buffer> res = event.result();
      RingResponseField[] fields = RingResponseField.values();
      var values = new Object[fields.length * 2];
      var valueIndex = 0;
      for (var i = 0; i < fields.length; i++) {
        var field = fields[i];
        Object v = field.from(res);
        if (v != null) {
          values[valueIndex] = field.keyword();
          values[valueIndex + 1] = v;
          valueIndex += 2;
        }
      }
      if (valueIndex == values.length) {
        promise.complete(toPersistentMap(values));
      } else {
        var copy = new Object[valueIndex];
        System.arraycopy(values, 0, copy, 0, valueIndex);
        promise.complete(toPersistentMap(copy));
      }
      
    } else {
      promise.fail(event.cause());
    }
  }
}
