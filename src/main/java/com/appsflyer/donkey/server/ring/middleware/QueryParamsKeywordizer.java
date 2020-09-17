package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.*;

import java.util.Objects;

public class QueryParamsKeywordizer implements RingMiddleware {
  
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  private static final Object NOT_FOUND = new Object();
  
  private static class MiddlewareHolder {
    
    private static final RingMiddleware instance = new QueryParamsKeywordizer();
    
  }
  
  public static RingMiddleware getInstance() {
    return MiddlewareHolder.instance;
  }
  
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    
    var localRequest = request;
    Object queryParams = localRequest.valAt(QUERY_PARAMS, NOT_FOUND);
    if (queryParams == NOT_FOUND) {
      localRequest = QueryParamsParser.getInstance().handle(localRequest);
      queryParams = localRequest.valAt(QUERY_PARAMS);
    }
    
    if (queryParams == null) {
      return localRequest;
    }
    
    int size = ((Counted) queryParams).count();
    if (size == 0) {
      return localRequest;
    }
    
    IPersistentMap params = (IPersistentMap) queryParams;
    Object[] res = new Object[params.count() << 1];
    var iter = params.iterator();
    for (int i = 0; iter.hasNext(); i += 2) {
      var entry = (IMapEntry) iter.next();
      res[i] = Keyword.intern((String) entry.getKey());
      res[i + 1] = entry.getValue();
    }
    return localRequest.assoc(QUERY_PARAMS, RT.mapUniqueKeys(res));
  }
}
