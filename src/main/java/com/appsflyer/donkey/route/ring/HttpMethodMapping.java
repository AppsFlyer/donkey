package com.appsflyer.donkey.route.ring;

import clojure.lang.Keyword;
import io.vertx.core.http.HttpMethod;

import java.util.Locale;

public final class HttpMethodMapping {
  
  private static final Keyword[] keywords = {
      Keyword.intern("options"),
      Keyword.intern("get"),
      Keyword.intern("head"),
      Keyword.intern("post"),
      Keyword.intern("put"),
      Keyword.intern("delete"),
      Keyword.intern("trace"),
      Keyword.intern("connect"),
      Keyword.intern("patch")
  };
  
  private HttpMethodMapping() {}
  
  public static Keyword get(HttpMethod method) {
    return keywords[method.ordinal()];
  }
  
  public static HttpMethod get(Keyword keyword) {
    return HttpMethod.valueOf(keyword.getName().toUpperCase(Locale.ENGLISH));
  }
}
