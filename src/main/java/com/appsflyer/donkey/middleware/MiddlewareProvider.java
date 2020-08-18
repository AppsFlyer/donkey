package com.appsflyer.donkey.middleware;

import clojure.lang.Counted;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.MapEntry;
import com.appsflyer.donkey.route.handler.ring.ClojureMutableHashMap;

import java.util.HashMap;
import java.util.Map;

public final class MiddlewareProvider
{
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  
  private MiddlewareProvider() {}
  
  public static IPersistentMap keywordizeQueryParams(IPersistentMap request)
  {
    Object queryParams = request.valAt(QUERY_PARAMS);
    if (queryParams == null) {
      return request;
    }
    int size = ((Counted) queryParams).count();
    if (size == 0) {
      return request;
    }
    Map<?, ?> params = (Map<?, ?>) queryParams;
    Map<Object, Object> newParams = new HashMap<>(params.size());
    for (Object obj : params.entrySet()) {
      MapEntry entry = (MapEntry) obj;
      newParams.put(Keyword.intern((String) entry.getKey()), entry.getValue());
    }
    return request.assoc(QUERY_PARAMS, new ClojureMutableHashMap<>(newParams));
  }
  
}
