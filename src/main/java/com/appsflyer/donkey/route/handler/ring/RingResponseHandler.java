package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.route.handler.ResponseBuilder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.appsflyer.donkey.route.handler.ring.Constants.RING_RESPONSE_FIELD;

public class RingResponseHandler extends ResponseBuilder implements Handler<RoutingContext>
{
  private static final Keyword HEADERS = Keyword.intern("headers");
  private static final Keyword BODY = Keyword.intern("body");
  private static final Keyword STATUS = Keyword.intern("status");
  
  RingResponseHandler(Vertx vertx)
  {
    super(vertx);
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    IPersistentMap ringResponse = ctx.get(RING_RESPONSE_FIELD);
    HttpServerResponse serverResponse = ctx.response();
    addDefaultHeaders(serverResponse);
    Iterable<?> headers = (Iterable<?>) ringResponse.valAt(HEADERS);
    if (headers != null) {
      for (var obj : headers) {
        var pair = (Map.Entry<?, ?>) obj;
        serverResponse.putHeader((CharSequence) pair.getKey(),
                                 (CharSequence) pair.getValue());
      }
    }
    serverResponse.setStatusCode(((Number) ringResponse.valAt(STATUS, 200)).intValue());
    
    Object body = ringResponse.valAt(BODY);
    if (body == null) {
      serverResponse.end();
    }
    else {
      serverResponse.end(Buffer.buffer(coerceToBytes(body)));
    }
  }
  
  private byte[] coerceToBytes(Object value)
  {
    if (value instanceof byte[]) {
      return (byte[]) value;
    }
    if (value instanceof String) {
      return (((String) value).getBytes(StandardCharsets.UTF_8));
    }
    throw new IllegalArgumentException(String.format("Cannot coerce %s into a byte[]", value.getClass().getName()));
  }
}
