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

import java.time.Duration;
import java.util.Objects;

public class StaticResourcesConfig {
  
  private String resourcesRoot;
  private String indexPage;
  private boolean enableCaching;
  private Duration maxAge;
  private Duration localCacheDuration;
  private int localCacheSize;
  
  /**
   * The directory from which resources are served.
   * The directory needs to be on the classpath.
   * Most commonly it should be under the {@code resources} directory
   * Defaults to {@code webroot}
   */
  String resourcesRoot() {
    return resourcesRoot;
  }
  
  /**
   * The file to serve when a directory is requested.
   * Defaults to index.html
   */
  String indexPage() {
    return indexPage;
  }
  
  /**
   * When caching is not enabled, then the server doesn't handle any of
   * the caching directive in the request (i.e Cache-Control header).
   */
  boolean enableCaching() {
    return enableCaching;
  }
  
  /**
   * The duration of time to tell a client to cache a resource.
   * Corresponds to the `max-age` directive of the Cache-Control header.
   * Ignored when caching is disabled.
   */
  Duration maxAge() {
    return maxAge;
  }
  
  /**
   * The handler keeps a local cache of file properties so it doesn't need to
   * access the filesystem on every request. This setting determines when an
   * entry becomes stale and the handler should invalidate the entry.
   * The best value depends on how frequently the assets change.
   * The duration is measured from the time the entry was added to the cache.
   */
  Duration localCacheDuration() {
    return localCacheDuration;
  }
  
  /**
   * The maximum number of items to store in the file properties cache
   */
  int localCacheSize() {
    return localCacheSize;
  }
  
  public static class Builder {
    
    private static final char ROOT_CHAR = '/';
    
    private static String ensureLeadingSlash(String path) {
      if (path.charAt(0) != ROOT_CHAR) {
        return ROOT_CHAR + path;
      }
      return path;
    }
    
    private StaticResourcesConfig instance;
    
    public Builder() {
      instance = new StaticResourcesConfig();
    }
    
    public Builder resourcesRoot(String resourcesRoot) {
      Objects.requireNonNull(resourcesRoot, "resourcesRoot cannot be null");
      if (resourcesRoot.isEmpty()) {
        throw new IllegalArgumentException("resourcesRoot cannot be empty");
      }
      instance.resourcesRoot = resourcesRoot;
      return this;
    }
    
    public Builder indexPage(String indexPage) {
      Objects.requireNonNull(indexPage, "indexPage cannot be null");
      if (indexPage.isEmpty()) {
        throw new IllegalArgumentException("indexPage cannot be empty");
      }
      instance.indexPage = ensureLeadingSlash(indexPage);
      return this;
    }
    
    
    public Builder enableCaching(boolean enable) {
      instance.enableCaching = enable;
      return this;
    }
    
    public Builder localCacheDuration(Duration duration) {
      Objects.requireNonNull(duration, "localCacheDuration cannot be null");
      instance.localCacheDuration = duration;
      return this;
    }
    
    public Builder maxAge(Duration duration) {
      Objects.requireNonNull(duration, "max-age duration cannot be null");
      instance.maxAge = duration;
      return this;
    }
    
    public Builder localCacheSize(int size) {
      if (size < 1) {
        throw new IllegalArgumentException("local cache size must be greater than 0");
      }
      instance.localCacheSize = size;
      return this;
    }
    
    public StaticResourcesConfig build() {
      var res = instance;
      instance = null;
      return res;
    }
  }
}
