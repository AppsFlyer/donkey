/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.route;

/**
 * Describes the contract between the application code handler and the library.
 * <p>
 * Handlers are typically executed on an event loop -
 * i.e by default handler mode is {@code HandlerMode.NON_BLOCKING}.
 * The event loop is also responsible for serving requests. It is essential
 * that it is not blocked while executing application code handlers.
 * <p>
 * In cases where handlers run blocking code, which is loosely described as any
 * operation that takes more than a few microseconds, then the
 * {@code HandlerMode.BLOCKING} mode should be used.
 * In that case the handler execution will be offloaded to a separate thread pool
 * leaving the event loop free to serve requests.
 */
public enum HandlerMode {
  BLOCKING, NON_BLOCKING
}
