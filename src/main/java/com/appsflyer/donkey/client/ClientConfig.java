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

package com.appsflyer.donkey.client;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Objects;

public final class ClientConfig {
  
  public static ClientConfigBuilder builder() {
    return new ClientConfigBuilder();
  }
  
  private Vertx vertx;
  private WebClientOptions clientOptions;
  private boolean debug;
  
  private ClientConfig() {}
  
  public Vertx vertx() {
    return vertx;
  }
  
  public WebClientOptions clientOptions() {
    return clientOptions;
  }
  
  public boolean debug() {
    return debug;
  }
  
  public static final class ClientConfigBuilder {
    
    private ClientConfig instance;
    
    private ClientConfigBuilder() {
      instance = new ClientConfig();
    }
    
    public ClientConfigBuilder vertx(Vertx vertx) {
      Objects.requireNonNull(vertx, "Vertx argument cannot be null");
      instance.vertx = vertx;
      return this;
    }
    
    public ClientConfigBuilder clientOptions(WebClientOptions clientOptions) {
      Objects.requireNonNull(clientOptions, "Client options argument cannot be null");
      instance.clientOptions = clientOptions;
      return this;
    }
    
    public ClientConfigBuilder debug(boolean val) {
      instance.debug = val;
      return this;
    }
    
    public ClientConfig build() {
      assertValidState();
      var res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState() {
      Objects.requireNonNull(instance.vertx, "Vertx field is missing");
      Objects.requireNonNull(instance.clientOptions, "Client options field is missing");
    }
  }
}
