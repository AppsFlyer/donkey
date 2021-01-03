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

package com.appsflyer.donkey.util;

import com.appsflyer.donkey.log.LogbackDelegateFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugUtilTest {
  
  @Test
  void testEnablingAndDisablingLogger() {
    var loggerFactory = new LogbackDelegateFactory();
    DebugUtil.disable();
    var loggers = List.of(
        Logger.ROOT_LOGGER_NAME,
        "io.netty",
        "io.vertx",
        "com.appsflyer").stream()
                      .map(loggerFactory::createDelegate)
                      .collect(Collectors.toList());
    
    loggers.forEach(
        logger -> assertFalse(
            logger.isDebugEnabled(),
            String.format("Logger %s debug is enabled", logger.getName())));
    
    DebugUtil.enable();
    
    loggers.forEach(
        logger -> assertTrue(
            logger.isDebugEnabled(),
            String.format("Logger %s debug is disabled", logger.getName())));
    
    DebugUtil.disable();
  }
}
