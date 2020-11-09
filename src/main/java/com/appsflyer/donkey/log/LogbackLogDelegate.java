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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.vertx.core.logging.SLF4JLogDelegate;

public class LogbackLogDelegate extends SLF4JLogDelegate {
  
  LogbackLogDelegate(String name) {
    super(name);
  }
  
  @SuppressWarnings("WeakerAccess")
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
