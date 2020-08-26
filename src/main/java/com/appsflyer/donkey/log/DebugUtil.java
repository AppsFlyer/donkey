package com.appsflyer.donkey.log;

import org.slf4j.Logger;

public final class DebugUtil {
  
  private static final String LOGBACK_LOGGER_FQCN = "ch.qos.logback.classic.Logger";
  
  private DebugUtil() {}
  
  public static void enableDebugLogging() {
    if (!isLogbackAvailable()) {
      return;
    }
    
    var root = LogbackLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    var vertx = LogbackLoggerFactory.getLogger("io.vertx");
    var netty = LogbackLoggerFactory.getLogger("io.netty");
    var appsflyer = LogbackLoggerFactory.getLogger("com.appsflyer");
    
    root.setLevel("TRACE");
    vertx.setLevel("TRACE");
    netty.setLevel("DEBUG");
    appsflyer.setLevel("DEBUG");
  }
  
  private static boolean isLogbackAvailable() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      loader.loadClass(LOGBACK_LOGGER_FQCN);
      return true;
    } catch (ClassNotFoundException ex) {
      //noinspection UseOfSystemOutOrSystemErr // We couldn't find the logger implementation so we assume there is none, and therefore print to stderr.
      System.err.printf("%s - Warning! Did not find %s on classpath. Debug logging will be disabled.%n",
                        DebugUtil.class.getCanonicalName(), LOGBACK_LOGGER_FQCN);
      return false;
    }
  }
}
