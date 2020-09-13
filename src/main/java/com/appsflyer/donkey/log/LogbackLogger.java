package com.appsflyer.donkey.log;

import io.vertx.core.logging.Logger;
import io.vertx.core.spi.logging.LogDelegate;

public class LogbackLogger extends Logger {
  
  public LogbackLogger(LogDelegate delegate) {
    super(delegate);
  }
  
  public String getName() {
    return ((LogbackLogDelegate) getDelegate()).getName();
  }
  
  public String getLevel() {
    return ((LogbackLogDelegate) getDelegate()).getLevel().toString();
  }
  
  public void setLevel(String level) {
    ((LogbackLogDelegate) getDelegate()).setLevel(level);
  }
}
