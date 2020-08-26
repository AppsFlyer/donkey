package com.appsflyer.donkey.util;

import com.appsflyer.donkey.log.LogbackLoggerFactory;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DebugUtil {
  
  private static final String LOGBACK_LOGGER_FQCN = "ch.qos.logback.classic.Logger";
  private static final String LEAK_DETECTION_LEVEL = "io.netty.leakDetection.level";
  private static final AtomicBoolean logbackAvailabilityCheck = new AtomicBoolean();
  private static final AtomicBoolean logbackAvailable = new AtomicBoolean();
  
  private DebugUtil() {}
  
  public static void enable() {
    enableDebugLogging();
    System.setProperty(LEAK_DETECTION_LEVEL, ResourceLeakDetector.Level.SIMPLE.name().toLowerCase());
  }
  
  private static void enableDebugLogging() {
    checkLogbackAvailability();
    if (!logbackAvailable.get()) {
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
  
  private static void checkLogbackAvailability() {
    if (!logbackAvailabilityCheck.get()) {
      logbackAvailabilityCheck.set(true);
      if (isLogbackAvailable()) {
        logbackAvailable.set(true);
      }
    }
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
