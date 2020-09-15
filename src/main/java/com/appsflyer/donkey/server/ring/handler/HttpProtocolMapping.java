package com.appsflyer.donkey.server.ring.handler;

import io.vertx.core.http.HttpVersion;

public final class HttpProtocolMapping {
  
  private static final String[] values = {"HTTP/1.0", "HTTP/1.1", "HTTP/2"};
  
  private HttpProtocolMapping() {}
  
  public static String get(HttpVersion version) {
    return values[version.ordinal()];
  }
}
