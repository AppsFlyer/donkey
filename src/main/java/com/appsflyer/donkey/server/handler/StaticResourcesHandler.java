/*
 * Copyright 2020-2021 AppsFlyer
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

package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Handler used to serve static assets.
 * It is recommend to use it rather than writing your own handler because
 * it handles caching file system properties and cache negotiation with client.
 */
public final class StaticResourcesHandler implements Handler<RoutingContext> {
  
  public static StaticResourcesHandler create(StaticResourcesConfig config) {
    return new StaticResourcesHandler(config);
  }
  
  private final StaticHandler impl;
  
  @SuppressWarnings("MethodWithMoreThanThreeNegations")
  private StaticResourcesHandler(StaticResourcesConfig config) {
    impl = StaticHandler.create();
    impl.setIncludeHidden(false);
  
    if (config.resourcesRoot() != null) {
      impl.setWebRoot(config.resourcesRoot());
    }
    if (config.indexPage() != null) {
      impl.setIndexPage(config.indexPage());
    }
  
    if (config.enableCaching()) {
      impl.setCachingEnabled(true);
      if (config.localCacheDuration() != null) {
        impl.setCacheEntryTimeout(config.localCacheDuration().toSeconds());
      }
      if (config.localCacheSize() > 0) {
        impl.setMaxCacheSize(config.localCacheSize());
      }
      if (config.maxAge() != null) {
        impl.setMaxAgeSeconds(config.maxAge().toSeconds());
      }
    } else {
      impl.setCachingEnabled(false);
    }
  }
  
  @Override
  public void handle(RoutingContext ctx) {
    impl.handle(ctx);
  }
  
}
