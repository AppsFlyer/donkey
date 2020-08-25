package com.appsflyer.donkey.log;

import org.slf4j.Logger;

public final class DebugUtil {
  
  private DebugUtil() {}
  
  public static void enableDebugLogging() {
    var root = LogbackLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    var vertx = LogbackLoggerFactory.getLogger("io.vertx");
    var netty = LogbackLoggerFactory.getLogger("io.netty");
    var appsflyer = LogbackLoggerFactory.getLogger("com.appsflyer");
    
    root.setLevel("TRACE");
    vertx.setLevel("TRACE");
    netty.setLevel("DEBUG");
    appsflyer.setLevel("DEBUG");
  }
}
