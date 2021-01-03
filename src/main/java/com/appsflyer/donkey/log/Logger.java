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

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 */
@SuppressWarnings("ClassWithTooManyMethods")
public class Logger {
  
  private final LogDelegate delegate;
  
  Logger(LogDelegate delegate) {
    this.delegate = delegate;
  }
  
  
  public boolean isWarnEnabled() {
    return delegate.isWarnEnabled();
  }
  
  public boolean isInfoEnabled() {
    return delegate.isInfoEnabled();
  }
  
  public boolean isDebugEnabled() {
    return delegate.isDebugEnabled();
  }
  
  public boolean isTraceEnabled() {
    return delegate.isTraceEnabled();
  }
  
  public void fatal(Object message) {
    delegate.fatal(message);
  }
  
  public void fatal(Object message, Throwable t) {
    delegate.fatal(message, t);
  }
  
  public void error(Object message) {
    delegate.error(message);
  }
  
  public void error(Object message, Throwable t) {
    delegate.error(message, t);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void error(Object message, Object... objects) {
    delegate.error(message, objects);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void error(Object message, Throwable t, Object... objects) {
    delegate.error(message, t, objects);
  }
  
  public void warn(Object message) {
    delegate.warn(message);
  }
  
  public void warn(Object message, Throwable t) {
    delegate.warn(message, t);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void warn(Object message, Object... objects) {
    delegate.warn(message, objects);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void warn(Object message, Throwable t, Object... objects) {
    delegate.warn(message, t, objects);
  }
  
  public void info(Object message) {
    delegate.info(message);
  }
  
  public void info(Object message, Throwable t) {
    delegate.info(message, t);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void info(Object message, Object... objects) {
    delegate.info(message, objects);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void info(Object message, Throwable t, Object... objects) {
    delegate.info(message, t, objects);
  }
  
  public void debug(Object message) {
    delegate.debug(message);
  }
  
  
  public void debug(Object message, Throwable t) {
    delegate.debug(message, t);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void debug(Object message, Object... objects) {
    delegate.debug(message, objects);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void debug(Object message, Throwable t, Object... objects) {
    delegate.debug(message, t, objects);
  }
  
  public void trace(Object message) {
    delegate.trace(message);
  }
  
  public void trace(Object message, Throwable t) {
    delegate.trace(message, t);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void trace(Object message, Object... objects) {
    delegate.trace(message, objects);
  }
  
  /**
   * @throws UnsupportedOperationException if the logging backend does not support parameterized messages
   */
  public void trace(Object message, Throwable t, Object... objects) {
    delegate.trace(message, t, objects);
  }
  
  /**
   * @return the delegate instance sending operations to the underlying logging framework
   */
  LogDelegate getDelegate() {
    return delegate;
  }
  
  public String getName() {
    return delegate.getName();
  }
  
  public String getLevel() {
    return delegate.getLevel();
  }
  
  public void setLevel(String level) {
    delegate.setLevel(level);
  }
}
