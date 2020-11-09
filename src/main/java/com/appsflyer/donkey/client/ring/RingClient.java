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
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

public interface RingClient extends Client<IPersistentMap> {
  
  static RingClient create(ClientConfig config) {
    return new RingClientImpl(config);
  }
  
  Future<IPersistentMap> send(HttpRequest<Buffer> request, Object body);
  
  Future<IPersistentMap> sendForm(HttpRequest<Buffer> request, IPersistentMap body);
  
  Future<IPersistentMap> sendMultiPartForm(HttpRequest<Buffer> request, IPersistentMap body);
}
