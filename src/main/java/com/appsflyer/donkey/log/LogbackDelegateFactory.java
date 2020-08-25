package com.appsflyer.donkey.log;

import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.core.spi.logging.LogDelegateFactory;
import org.slf4j.LoggerFactory;


@SuppressWarnings("unused") //Class is loaded dynamically
public class LogbackDelegateFactory implements LogDelegateFactory {
  
  public LogDelegate createDelegate(String clazz) {
    return new LogbackLogDelegate(LoggerFactory.getLogger(clazz));
  }
}
