package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import com.appsflyer.donkey.server.ring.handler.RingRequestField;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.appsflyer.donkey.util.TypeConverter.toVector;

public class QueryParamsParser implements RingMiddleware {
  
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  private static final Keyword QUERY_STRING = RingRequestField.QUERY_STRING.keyword();
  
  private static class MiddlewareHolder {
    
    private static final RingMiddleware instance = new QueryParamsParser();
    
  }
  
  public static RingMiddleware getInstance() {
    return MiddlewareHolder.instance;
  }
  
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    
    if (request.containsKey(QUERY_PARAMS)) {
      return request;
    }
    
    var queryString = (String) request.valAt(QUERY_STRING);
    if (queryString == null || queryString.isBlank()) {
      return request;
    }
    
    Map<String, List<String>> decodedParams =
        new QueryStringDecoder(queryString, false).parameters();
    
    Object[] values = new Object[decodedParams.size() << 1];
    int i = 0;
    for (Map.Entry<String, List<String>> entry : decodedParams.entrySet()) {
      values[i] = entry.getKey();
      List<String> val = entry.getValue();
      if (val.size() > 1) {
        values[i + 1] = toVector(val);
      } else {
        values[i + 1] = val.get(0);
      }
      i += 2;
    }
    
    return request.assoc(QUERY_PARAMS, RT.mapUniqueKeys(values));
  }
}
