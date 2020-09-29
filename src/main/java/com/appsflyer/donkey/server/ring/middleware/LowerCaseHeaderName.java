package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;

import java.util.Locale;
import java.util.Objects;

import static com.appsflyer.donkey.server.ring.handler.RingRequestField.HEADERS;

public class LowerCaseHeaderName implements RingMiddleware {
  
  private static class MiddlewareHolder {
    
    private static final RingMiddleware instance = new LowerCaseHeaderName();
  }
  
  public static RingMiddleware getInstance() {
    return MiddlewareHolder.instance;
  }
  
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    
    var val = request.valAt(HEADERS.keyword(), null);
    if (val == null) {
      return request;
    }
    
    IPersistentMap headers = (IPersistentMap) val;
    Object[] res = new Object[headers.count() * 2];
    var iter = headers.iterator();
    for (int i = 0; iter.hasNext(); i += 2) {
      var entry = (IMapEntry) iter.next();
      res[i] = ((String) entry.getKey()).toLowerCase(Locale.ROOT);
      res[i + 1] = entry.getValue();
    }
    
    return request.assoc(HEADERS.keyword(), RT.mapUniqueKeys(res));
  }
}
