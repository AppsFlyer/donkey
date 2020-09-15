package com.appsflyer.donkey.server.ring.handler;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import static com.appsflyer.donkey.server.ring.handler.RingHandler.LAST_HANDLER_RESPONSE_FIELD;
import static com.appsflyer.donkey.server.ring.handler.RingResponseField.*;
import static com.appsflyer.donkey.util.TypeConverter.toBuffer;

public class RingResponseAdapter implements Handler<RoutingContext> {
  
  @Override
  public void handle(RoutingContext ctx) {
    IPersistentMap ringResponse = ctx.get(LAST_HANDLER_RESPONSE_FIELD);
    
    if (ringResponse == null) {
      ctx.response().end();
      return;
    }
    
    HttpServerResponse serverResponse = ctx.response();
    addHeaders(serverResponse, ringResponse);
    setStatus(serverResponse, ringResponse);
    sendResponse(serverResponse, ringResponse);
  }
  
  private void addHeaders(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    var headers = (IPersistentMap) HEADERS.from(ringResponse);
    if (headers != null) {
      for (var obj : headers) {
        var pair = (IMapEntry) obj;
        serverResponse.putHeader((CharSequence) pair.getKey(), (CharSequence) pair.getValue());
      }
    }
  }
  
  private void setStatus(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    serverResponse.setStatusCode((Integer) STATUS.from(ringResponse));
  }
  
  private void sendResponse(HttpServerResponse serverResponse, IPersistentMap ringResponse) {
    serverResponse.end(toBuffer(BODY.from(ringResponse)));
  }
}
