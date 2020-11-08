package com.appsflyer.donkey.server.ring.handler;

import clojure.lang.Keyword;
import io.vertx.core.http.HttpMethod;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class HttpMethodMapping {
  
  private static final Map<HttpMethod, Keyword> enumToKeyword = new EnumMap<>(HttpMethod.class);
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
