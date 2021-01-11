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

package com.appsflyer.donkey;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VertxFactory {
  
  private static final Logger logger = LoggerFactory.getLogger(VertxFactory.class.getName());
  
  private VertxFactory() {}
  
  public static Vertx create(VertxOptions opts) {
    return Vertx.vertx(opts)
                .exceptionHandler(VertxFactory::defaultExceptionHandler);
  }
  
  private static void defaultExceptionHandler(Throwable ex) {
    Throwable t = ex;
    if (logger.isDebugEnabled()) {
      var stack = ex.getStackTrace();
      // If we don't have a stack trace we fill it now
      if (stack == null || stack.length == 0) {
        t = new RuntimeException(ex);
      }
      logger.debug(ex.getMessage(), t);
    } else {
      logger.error(ex.getMessage(), ex);
    }
  }
}
