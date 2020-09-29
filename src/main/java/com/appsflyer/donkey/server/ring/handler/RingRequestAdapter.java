package com.appsflyer.donkey.server.ring.handler;

import io.vertx.ext.web.RoutingContext;

import static com.appsflyer.donkey.util.TypeConverter.toPersistentMap;

/**
 * Handler responsible for converting an {@link io.vertx.core.http.HttpServerRequest}
 * to a Ring compliant Clojure map.
 * <p></p>
 * See the Ring <a href="https://github.com/ring-clojure/ring/blob/master/SPEC">specification</a> for more details.
 */
public class RingRequestAdapter implements RingHandler {
  
  @Override
  public void handle(RoutingContext ctx) {
    RingRequestField[] fields = RingRequestField.values();
    var values = new Object[fields.length * 2];
    
    var j = 0;
    for (int i = 0; i < fields.length; i++) {
      var field = fields[i];
      Object v = field.from(ctx);
      if (v != null) {
        values[j] = field.keyword();
        values[j + 1] = v;
        j += 2;
      }
    }
    
    if (j == values.length) {
      ctx.put(RING_HANDLER_RESULT, toPersistentMap(values));
    } else {
      var copy = new Object[j];
      System.arraycopy(values, 0, copy, 0, j);
      ctx.put(RING_HANDLER_RESULT, toPersistentMap(copy));
    }
    
    ctx.next();
  }
}
