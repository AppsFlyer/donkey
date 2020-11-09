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

public class ClientConfigImpl implements ClientConfig {
  
  private Vertx vertx;
  private WebClientOptions clientOptions;
  private boolean debug;
  
  @Override
  public Vertx vertx() {
    return vertx;
  }
  
  @Override
  public WebClientOptions clientOptions() {
    return clientOptions;
  }
  
  @Override
  public boolean debug() {
    return debug;
  }
  
  public static final class ClientConfigBuilderImpl implements ClientConfigBuilder {
    
    private ClientConfigImpl instance;
    
    ClientConfigBuilderImpl() {
      instance = new ClientConfigImpl();
    }
    
    @Override
    public ClientConfigBuilder vertx(Vertx vertx) {
      Objects.requireNonNull(vertx, "Vertx argument cannot be null");
      instance.vertx = vertx;
      return this;
    }
    
    @Override
    public ClientConfigBuilder clientOptions(WebClientOptions clientOptions) {
      Objects.requireNonNull(clientOptions, "Client options argument cannot be null");
      instance.clientOptions = clientOptions;
      return this;
    }
    
    @Override
    public ClientConfigBuilder debug(boolean val) {
      instance.debug = val;
      return this;
    }
    
    @Override
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
