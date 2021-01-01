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

package com.appsflyer.donkey.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import static org.slf4j.spi.LocationAwareLogger.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@SuppressWarnings("ClassWithTooManyMethods")
abstract class SLF4JLogDelegate implements LogDelegate {
  
  private static final String FQCN = Logger.class.getCanonicalName();
  
  private final Logger logger;
  
  SLF4JLogDelegate(String name) {
    logger = LoggerFactory.getLogger(name);
  }
  
  SLF4JLogDelegate(Object logger) {
    this.logger = (Logger) logger;
  }
  
  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }
  
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }
  
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }
  
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }
  
  public void fatal(Object message) {
    log(ERROR_INT, message);
  }
  
  public void fatal(Object message, Throwable t) {
    log(ERROR_INT, message, t);
  }
  
  public void error(Object message) {
    log(ERROR_INT, message);
  }
  
  @Override
  public void error(Object message, Object... params) {
    log(ERROR_INT, message, null, params);
  }
  
  public void error(Object message, Throwable t) {
    log(ERROR_INT, message, t);
  }
  
  @Override
  public void error(Object message, Throwable t, Object... params) {
    log(ERROR_INT, message, t, params);
  }
  
  public void warn(Object message) {
    log(WARN_INT, message);
  }
  
  @Override
  public void warn(Object message, Object... params) {
    log(WARN_INT, message, null, params);
  }
  
  public void warn(Object message, Throwable t) {
    log(WARN_INT, message, t);
  }
  
  @Override
  public void warn(Object message, Throwable t, Object... params) {
    log(WARN_INT, message, t, params);
  }
  
  public void info(Object message) {
    log(INFO_INT, message);
  }
  
  @Override
  public void info(Object message, Object... params) {
    log(INFO_INT, message, null, params);
  }
  
  public void info(Object message, Throwable t) {
    log(INFO_INT, message, t);
  }
  
  @Override
  public void info(Object message, Throwable t, Object... params) {
    log(INFO_INT, message, t, params);
  }
  
  public void debug(Object message) {
    log(DEBUG_INT, message);
  }
  
  public void debug(Object message, Object... params) {
    log(DEBUG_INT, message, null, params);
  }
  
  public void debug(Object message, Throwable t) {
    log(DEBUG_INT, message, t);
  }
  
  public void debug(Object message, Throwable t, Object... params) {
    log(DEBUG_INT, message, t, params);
  }
  
  public void trace(Object message) {
    log(TRACE_INT, message);
  }
  
  @Override
  public void trace(Object message, Object... params) {
    log(TRACE_INT, message, null, params);
  }
  
  public void trace(Object message, Throwable t) {
    log(TRACE_INT, message, t);
  }
  
  @Override
  public void trace(Object message, Throwable t, Object... params) {
    log(TRACE_INT, message, t, params);
  }
  
  private void log(int level, Object message) {
    log(level, message, null);
  }
  
  private void log(int level, Object message, Throwable t) {
    log(level, message, t, (Object[]) null);
  }
  
  private void log(int level, Object message, Throwable t, Object... params) {
    String msg = (message == null) ? "NULL" : message.toString();
    
    // We need to compute the right parameters.
    // If we have both parameters and an error, we need to build a new array [params, t]
    // If we don't have parameters, we need to build a new array [t]
    // If we don't have error, it's just params.
    Object[] parameters = params;
    if (params != null && t != null) {
      parameters = new Object[params.length + 1];
      System.arraycopy(params, 0, parameters, 0, params.length);
      parameters[params.length] = t;
    } else if (params == null && t != null) {
      parameters = new Object[]{t};
    }
    
    if (logger instanceof LocationAwareLogger) {
      // make sure we don't format the objects if we don't log the line anyway
      if (level == TRACE_INT && logger.isTraceEnabled() ||
          level == DEBUG_INT && logger.isDebugEnabled() ||
          level == INFO_INT && logger.isInfoEnabled() ||
          level == WARN_INT && logger.isWarnEnabled() ||
          level == ERROR_INT && logger.isErrorEnabled()) {
        LocationAwareLogger l = (LocationAwareLogger) logger;
        FormattingTuple ft = MessageFormatter.arrayFormat(msg, parameters);
        l.log(null, FQCN, level, ft.getMessage(), null, ft.getThrowable());
      }
    } else {
      switch (level) {
        case TRACE_INT:
          logger.trace(msg, parameters);
          break;
        case DEBUG_INT:
          logger.debug(msg, parameters);
          break;
        case INFO_INT:
          logger.info(msg, parameters);
          break;
        case WARN_INT:
          logger.warn(msg, parameters);
          break;
        case ERROR_INT:
          logger.error(msg, parameters);
          break;
        default:
          throw new IllegalArgumentException(String.format("Unknown log level %d", level));
      }
    }
  }
  
  @Override
  public Object unwrap() {
    return logger;
  }
  
}
