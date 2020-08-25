package com.appsflyer.donkey.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.vertx.core.logging.SLF4JLogDelegate;

public class LogbackLogDelegate extends SLF4JLogDelegate {
  
  LogbackLogDelegate(String name) {
    super(name);
  }
  
  public LogbackLogDelegate(Object logger) {
    super(logger);
  }
  
  public void setLevel(String level) {
    ((Logger) unwrap()).setLevel(Level.toLevel(level));
  }
}
