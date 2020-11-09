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

public class FormParamsKeywordizer implements RingMiddleware {
  
  private static final Keyword FORM_PARAMS = Keyword.intern("form-params");
  private final Options options;
  
  public static class Options {
    
    private final boolean deep;
    
    public Options(boolean deep) {this.deep = deep;}
  }
  
  public FormParamsKeywordizer(Options options) {
    this.options = options;
  }
  
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    
    Object formParams = request.valAt(FORM_PARAMS, null);
    
    if (formParams == null) {
      return request;
    }
    
    int size = ((Counted) formParams).count();
    if (size == 0) {
      return request;
    }
    return request.assoc(FORM_PARAMS, keywordize((IPersistentMap) formParams));
  }
  
  private IPersistentMap keywordize(IPersistentMap map) {
    Object[] res = new Object[map.count() * 2];
    var iter = map.iterator();
    for (int i = 0; iter.hasNext(); i += 2) {
      var entry = (IMapEntry) iter.next();
      res[i] = Keyword.intern((String) entry.getKey());
      if (options.deep && (entry.getValue() instanceof IPersistentMap)) {
        res[i + 1] = keywordize((IPersistentMap) entry.getValue());
      } else {
        res[i + 1] = entry.getValue();
      }
    }
    return RT.mapUniqueKeys(res);
  }
}
