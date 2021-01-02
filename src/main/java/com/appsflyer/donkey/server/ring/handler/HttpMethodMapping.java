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

package com.appsflyer.donkey.server.ring.handler;

import clojure.lang.Keyword;
import io.vertx.core.http.HttpMethod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class HttpMethodMapping {
  
  private static final Map<HttpMethod, Keyword> enumToKeyword = new HashMap<>();
  private static final Map<Keyword, HttpMethod> keywordToEnum = new HashMap<>();
  
  static {
    for (HttpMethod method : HttpMethod.values()) {
      var keyword = Keyword.intern(method.name().toLowerCase(Locale.ENGLISH));
      enumToKeyword.put(method, keyword);
      keywordToEnum.put(keyword, method);
    }
  }
  
  private HttpMethodMapping() {}
  
  public static Keyword get(HttpMethod method) {
    return enumToKeyword.get(method);
  }
  
  public static HttpMethod get(Keyword keyword) {
    return keywordToEnum.get(keyword);
  }
}
