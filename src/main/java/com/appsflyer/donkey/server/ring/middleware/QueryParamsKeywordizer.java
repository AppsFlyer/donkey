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
    Object[] res = new Object[params.count() * 2];
    var iter = params.iterator();
    for (int i = 0; iter.hasNext(); i += 2) {
      var entry = (IMapEntry) iter.next();
      res[i] = Keyword.intern((String) entry.getKey());
      res[i + 1] = entry.getValue();
    }
    return localRequest.assoc(QUERY_PARAMS, RT.mapUniqueKeys(res));
  }
}
