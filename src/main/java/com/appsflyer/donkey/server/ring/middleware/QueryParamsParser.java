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

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.appsflyer.donkey.server.ring.handler.RingRequestField.QUERY_STRING;
import static com.appsflyer.donkey.util.TypeConverter.toVector;

public class QueryParamsParser implements RingMiddleware {
  
  public static class Options {
    
    private static final Options DEFAULT = new Options();
    private boolean keywordizeKeys;
    
    public Options keywordizeKeys(boolean keywordizeKeys) {
      this.keywordizeKeys = keywordizeKeys;
      return this;
    }
  }
  
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  
  private final Function<String, ?> mappingFunc;
  
  public QueryParamsParser(Options opts) {
    mappingFunc = opts.keywordizeKeys ?
        Keyword::intern :
        Function.identity();
  }
  
  @SuppressWarnings("WeakerAccess")
  public QueryParamsParser() {
    this(Options.DEFAULT);
  }
  
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    
    if (request.containsKey(QUERY_PARAMS)) {
      return request;
    }
    
    var queryString = (String) request.valAt(QUERY_STRING.keyword(), null);
    if (queryString == null || queryString.isBlank()) {
      return request;
    }
    
    Map<String, List<String>> decodedParams =
        new QueryStringDecoder(queryString, false).parameters();
    
    Object[] values = new Object[decodedParams.size() * 2];
    int valueIndex = 0;
    for (Map.Entry<String, List<String>> entry : decodedParams.entrySet()) {
      values[valueIndex] = mappingFunc.apply(entry.getKey());
      List<String> val = entry.getValue();
      if (val.size() > 1) {
        values[valueIndex + 1] = toVector(val);
      } else {
        values[valueIndex + 1] = val.get(0);
      }
      valueIndex += 2;
    }
    
    return request.assoc(QUERY_PARAMS, RT.mapUniqueKeys(values));
  }
}
