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
 * I represent operations that are delegated to underlying logging frameworks.
 *
 * @author <a href="kenny.macleod@kizoom.com">Kenny MacLeod</a>
 */
@SuppressWarnings("ClassWithTooManyMethods")
public interface LogDelegate {
  
  boolean isWarnEnabled();
  
  boolean isInfoEnabled();
  
  boolean isDebugEnabled();
  
  boolean isTraceEnabled();
  
  void fatal(Object message);
  
  void fatal(Object message, Throwable t);
  
  void error(Object message);
  
  void error(Object message, Object... params);
  
  void error(Object message, Throwable t);
  
  void error(Object message, Throwable t, Object... params);
  
  void warn(Object message);
  
  void warn(Object message, Object... params);
  
  void warn(Object message, Throwable t);
  
  void warn(Object message, Throwable t, Object... params);
  
  void info(Object message);
  
  void info(Object message, Object... params);
  
  void info(Object message, Throwable t);
  
  void info(Object message, Throwable t, Object... params);
  
  void debug(Object message);
  
  void debug(Object message, Object... params);
  
  void debug(Object message, Throwable t);
  
  void debug(Object message, Throwable t, Object... params);
  
  void trace(Object message);
  
  void trace(Object message, Object... params);
  
  void trace(Object message, Throwable t);
  
  void trace(Object message, Throwable t, Object... params);
  
  /**
   * @return the underlying framework logger object
   */
  Object unwrap();
  
  
  String getName();
  
  String getLevel();
  
  void setLevel(String level);
}
