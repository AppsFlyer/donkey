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

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.multipart.MultipartForm;

/**
 * @param <T> The type of the request options object
 * @param <R> The type of the response
 */
public interface Client<T, R> {
  
  HttpRequest<Buffer> request(T opts);
  
  Future<R> send(HttpRequest<Buffer> request);
  
  Future<R> send(HttpRequest<Buffer> request, Buffer body);
  
  Future<R> sendForm(HttpRequest<Buffer> request, MultiMap body);
  
  Future<R> sendMultiPartForm(HttpRequest<Buffer> request, MultipartForm body);
  
  void shutdown();
}
