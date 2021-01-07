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

package com.appsflyer.donkey.server.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.BiConsumer;

/**
 * The interface represents a set of error handlers that are associated with
 * specific HTTP status codes.
 *
 * @param <T> The type of the handler function
 */
public interface ErrorHandler<T> {
  
  ErrorHandler<T> add(int statusCode, T handler);
  
  void forEach(BiConsumer<Integer, Handler<RoutingContext>> consumer);
}
