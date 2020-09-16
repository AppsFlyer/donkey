package com.appsflyer.donkey.server.ring.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static com.appsflyer.donkey.server.ring.handler.RingHandler.LAST_HANDLER_RESPONSE_FIELD;
import static com.appsflyer.donkey.util.TypeConverter.toPersistentMap;

/**
 * Handler responsible for converting an {@link io.vertx.core.http.HttpServerRequest}
 * to a Ring compliant Clojure map.
 * <p></p>
 * See the Ring <a href="https://github.com/ring-clojure/ring/blob/master/SPEC">specification</a> for more details.
 */
public class RingRequestAdapter implements Handler<RoutingContext> {
  
  @Override
  public void handle(RoutingContext ctx) {
    RingRequestField[] fields = RingRequestField.values();
    var values = new Object[fields.length << 1];
    var i = 0;
    for (RingRequestField field : fields) {
      Object v = field.from(ctx);
      if (v != null) {
        values[i] = field.keyword();
        values[i + 1] = v;
        i += 2;
      }
    }
  
    if (i == values.length) {
      ctx.put(LAST_HANDLER_RESPONSE_FIELD, toPersistentMap(values));
    } else {
      var copy = new Object[i];
      System.arraycopy(values, 0, copy, 0, i);
      ctx.put(LAST_HANDLER_RESPONSE_FIELD, toPersistentMap(copy));
    }
  
    ctx.next();
  }
}
