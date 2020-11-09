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
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.TimeUnit;

public interface Server {
  
  /**
   * Create a new instance with the given {@link ServerConfig}
   *
   * @param config See {@link ServerConfig} for configuration options.
   */
  static Server create(ServerConfig config) {
    return new ServerImpl(config);
  }
  
  /**
   * @return The Vertx instance associated with the server
   */
  Vertx vertx();
  
  /**
   * Deploy the server verticle.
   *
   * @return Future of the verticle deployment id
   */
  Future<String> start();
  
  /**
   * Deploy the server verticle.
   * Blocks the calling thread until the operation succeeds or fails.
   *
   * @throws ServerInitializationException If the thread was interrupted while deploying the verticle,
   *                                       the operation timed out or failed in some other way.
   */
  void startSync() throws ServerInitializationException;
  
  /**
   * Deploy the server verticle.
   * Blocks the calling thread until the operation succeeds or fails.
   *
   * @param timeout The duration of time to wait for the operation to complete.
   * @param unit    The duration time unit
   * @throws ServerInitializationException If the thread was interrupted while deploying the verticle,
   *                                       the operation timed out or failed in some other way.
   */
  void startSync(int timeout, TimeUnit unit) throws
                                             ServerInitializationException;
  
  /**
   * Stops the server by releasing all resources (threads, connections, etc') associated
   * with it.
   *
   * @return Future result that will contain the Exception in case the operation failed.
   */
  Future<Void> shutdown();
  
  /**
   * Stops the server while blocking the calling thread.
   * Returns when all resources (threads, connections, etc') are released.
   *
   * @throws ServerShutdownException If the thread was interrupted while shutting down,
   *                                 the operation timed out or failed in some other way.
   */
  void shutdownSync() throws ServerShutdownException;
  
  /**
   * Stops the server while blocking the calling thread up to the given timeout duration.
   * Returns when all resources (threads, connections, etc') are released.
   *
   * @param timeout The duration of time to wait for the operation to complete.
   * @param unit    The duration time unit
   * @throws ServerShutdownException If the thread was interrupted while shutting down,
   *                                 the operation timed out or failed in some other way.
   */
  void shutdownSync(int timeout, TimeUnit unit) throws
                                                ServerShutdownException;
}
