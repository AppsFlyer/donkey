package com.appsflyer.donkey.log;

import io.vertx.core.logging.Logger;
import io.vertx.core.spi.logging.LogDelegate;

public class LogbackLogger extends Logger {
  
  public LogbackLogger(LogDelegate delegate) {
    super(delegate);
  }
  
  public void setLevel(String level) {
    ((LogbackLogDelegate) getDelegate()).setLevel(level);
  }
}
