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

package com.appsflyer.donkey.util;

import com.appsflyer.donkey.log.LogbackLoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static io.netty.util.ResourceLeakDetector.Level.DISABLED;
import static io.netty.util.ResourceLeakDetector.Level.SIMPLE;

public final class DebugUtil {
  
  private static final String LOGBACK_LOGGER_FQCN = "ch.qos.logback.classic.Logger";
  private static final String LEAK_DETECTION_LEVEL = "io.netty.leakDetection.level";
  private static final AtomicBoolean logbackAvailabilityCheck = new AtomicBoolean();
  private static final AtomicBoolean logbackAvailable = new AtomicBoolean();
  private static final AtomicBoolean originalLevelsAvailable = new AtomicBoolean();
  private static final Map<String, String> originalLevels = new HashMap<>(4);
  private static final String LEAK_DETECTOR_SIMPLE = SIMPLE.name().toLowerCase();
  private static final String LEAK_DETECTOR_DISABLED = DISABLED.name().toLowerCase();
  private static final String LOGGER_NETTY = "io.netty";
  private static final String LOGGER_VERTX = "io.vertx";
  private static final String LOGGER_APPSFLYER = "com.appsflyer";
  
  
  private DebugUtil() {}
  
  public static void enable() {
    enableDebugLogging();
    System.setProperty(LEAK_DETECTION_LEVEL, LEAK_DETECTOR_SIMPLE);
  }
  
  public static void disable() {
    disableDebugLogging();
    System.setProperty(LEAK_DETECTION_LEVEL, LEAK_DETECTOR_DISABLED);
  }
  
  private static void enableDebugLogging() {
    checkLogbackAvailability(DebugUtil::isLogbackAvailableWithWarning);
    if (!logbackAvailable.get()) {
      return;
    }
    
    var root = LogbackLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    var vertx = LogbackLoggerFactory.getLogger(LOGGER_VERTX);
    var netty = LogbackLoggerFactory.getLogger(LOGGER_NETTY);
    var appsflyer = LogbackLoggerFactory.getLogger(LOGGER_APPSFLYER);
    
    if (!originalLevelsAvailable.get()) {
      //This is not a contended block that is only called at startup.
      //noinspection SynchronizationOnStaticField
      synchronized (originalLevels) {
        if (!originalLevelsAvailable.get()) {
          originalLevels.put(root.getName(), root.getLevel());
          originalLevels.put(vertx.getName(), vertx.getLevel());
          originalLevels.put(netty.getName(), netty.getLevel());
          originalLevels.put(appsflyer.getName(), appsflyer.getLevel());
          originalLevelsAvailable.set(true);
        }
      }
    }
    
    root.setLevel("TRACE");
    vertx.setLevel("TRACE");
    netty.setLevel("TRACE");
    appsflyer.setLevel("TRACE");
  }
  
  private static void disableDebugLogging() {
    checkLogbackAvailability(DebugUtil::isLogbackAvailable);
    if (!logbackAvailable.get()) {
      return;
    }
    
    var root = LogbackLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    var vertx = LogbackLoggerFactory.getLogger(LOGGER_VERTX);
    var netty = LogbackLoggerFactory.getLogger(LOGGER_NETTY);
    var appsflyer = LogbackLoggerFactory.getLogger(LOGGER_APPSFLYER);
    
    if (originalLevelsAvailable.get()) {
      //This block is called once at startup and is not a source of contention.
      //noinspection SynchronizationOnStaticField
      synchronized (originalLevels) {
        if (originalLevelsAvailable.get()) {
          root.setLevel(originalLevels.get(root.getName()));
          vertx.setLevel(originalLevels.get(vertx.getName()));
          netty.setLevel(originalLevels.get(netty.getName()));
          appsflyer.setLevel(originalLevels.get(appsflyer.getName()));
          originalLevels.clear();
          originalLevelsAvailable.set(false);
        }
      }
    }
  }
  
  private static void checkLogbackAvailability(Supplier<Boolean> availabilityCheck) {
    if (!logbackAvailabilityCheck.get()) {
      if (availabilityCheck.get()) {
        logbackAvailable.set(true);
      }
      logbackAvailabilityCheck.set(true);
    }
  }
  
  private static boolean loadLogback() throws ClassNotFoundException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    loader.loadClass(LOGBACK_LOGGER_FQCN);
    return true;
  }
  
  private static boolean isLogbackAvailable() {
    try {
      return loadLogback();
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }
  
  private static boolean isLogbackAvailableWithWarning() {
    try {
      return loadLogback();
    } catch (ClassNotFoundException ex) {
      //noinspection UseOfSystemOutOrSystemErr // We couldn't find the logger implementation so we assume there is none, and therefore print to stderr.
      System.err.printf("%s - Warning! Did not find %s on classpath. Debug logging will be disabled.%n",
                        DebugUtil.class.getCanonicalName(), LOGBACK_LOGGER_FQCN);
      return false;
    }
  }
}
