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
  
  public String getName() {
    return ((org.slf4j.Logger) unwrap()).getName();
  }
  
  public Level getLevel() {
    return ((Logger) unwrap()).getLevel();
  }
  
  public void setLevel(String level) {
    ((Logger) unwrap()).setLevel(Level.toLevel(level));
  }
}
