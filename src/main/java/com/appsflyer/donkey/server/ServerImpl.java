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

package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import com.appsflyer.donkey.server.handler.DateHeaderHandler;
import com.appsflyer.donkey.server.handler.ServerHeaderHandler;
import com.appsflyer.donkey.server.route.RouteDefinition;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ServerImpl implements Server {
  
  private static final int TIMEOUT_SECONDS = 10;
  private final ServerConfig config;
  
  /**
   * Create a new instance with the given {@link ServerConfig}
   *
   * @param config See {@link ServerConfig} for configuration options.
   */
  public static Server create(ServerConfig config) {
    return new ServerImpl(config);
  }
  
  private ServerImpl(ServerConfig config) {
    this.config = config;
    addOptionalHandlers();
  }
  
  private void addOptionalHandlers() {
    Collection<Handler<RoutingContext>> handlers = new ArrayList<>();
    if (config.addDateHeader()) {
      handlers.add(DateHeaderHandler.create(config.vertx()));
    }
    if (config.addContentTypeHeader()) {
      handlers.add(ResponseContentTypeHandler.create());
    }
    if (config.addServerHeader()) {
      handlers.add(ServerHeaderHandler.create());
    }
  
    handlers.forEach(h -> config.routeList().addFirst(RouteDefinition.create().handler(h)));
  }
  
  @Override
  public Vertx vertx() {
    return config.vertx();
  }
  
  @Override
  public Future<String> start() {
    return config.vertx()
                 .deployVerticle(
                     () -> new ServerVerticle(config),
                     new DeploymentOptions()
                         .setInstances(config.instances()));
  }
  
  @Override
  public void startSync() throws ServerInitializationException {
    startSync(TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }
  
  @Override
  public void startSync(int timeout, TimeUnit unit) throws
                                                    ServerInitializationException {
    var latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    
    start().onComplete(v -> {
      if (v.failed()) {
        error.set(v.cause());
      }
      latch.countDown();
    });
    
    try {
      if (!latch.await(timeout, unit)) {
        throw new ServerInitializationException(
            String.format("Server start up timed out after %d %s",
                          timeout, unit.name().toLowerCase(Locale.ENGLISH)));
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerInitializationException(
          "Thread interrupted during initialization", ex);
    }
    
    if (error.get() != null) {
      throw new ServerInitializationException(error.get());
    }
  }
  
  @Override
  public Future<Void> shutdown() {
    return config.vertx().close();
  }
  
  @Override
  public void shutdownSync() throws ServerShutdownException {
    shutdownSync(TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }
  
  @Override
  public void shutdownSync(int timeout, TimeUnit unit) throws
                                                       ServerShutdownException {
    var latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
  
    shutdown()
        .onFailure(error::set)
        .onComplete(v -> latch.countDown());
  
    try {
      if (!latch.await(timeout, unit)) {
        throw new ServerShutdownException(
            String.format("Server shutdown timed out after %d %s",
                          timeout, unit.name().toLowerCase(Locale.ENGLISH)));
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerShutdownException("Thread interrupted during shutdown", ex);
    }
    
    if (error.get() != null) {
      throw new ServerShutdownException(error.get());
    }
  }
}
