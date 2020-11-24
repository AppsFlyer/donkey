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

package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.Client;
import com.appsflyer.donkey.client.ClientConfig;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;

public final class RingClient implements Client<IPersistentMap, IPersistentMap> {
  
  public static RingClient create(ClientConfig config) {
    return new RingClient(config);
  }
  
  private final WebClient client;
  private final RingRequestFactory requestFactory;
  
  private RingClient(ClientConfig config) {
    client = WebClient.create(config.vertx(), config.clientOptions());
    requestFactory = RingRequestFactory.create(client);
  }
  
  public HttpRequest<Buffer> request(IPersistentMap opts) {
    return requestFactory.create(opts);
  }
  
  public Future<IPersistentMap> send(HttpRequest<Buffer> request) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.send(RingResponseAdapter.create(promise));
    return promise.future();
  }
  
  public Future<IPersistentMap> send(HttpRequest<Buffer> request, Buffer body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendBuffer(body, RingResponseAdapter.create(promise));
    return promise.future();
  }
  
  public Future<IPersistentMap> sendForm(HttpRequest<Buffer> request, MultiMap body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendForm(body, RingResponseAdapter.create(promise));
    return promise.future();
  }
  
  public Future<IPersistentMap> sendMultiPartForm(HttpRequest<Buffer> request, MultipartForm body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendMultipartForm(body, RingResponseAdapter.create(promise));
    return promise.future();
  }
  
  public void shutdown() {
    client.close();
  }
  
}
