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
public class RingResponseAdapter implements Handler<AsyncResult<HttpResponse<Buffer>>> {
  
  private final Promise<IPersistentMap> promise;
  
  RingResponseAdapter(Promise<IPersistentMap> promise) {
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
