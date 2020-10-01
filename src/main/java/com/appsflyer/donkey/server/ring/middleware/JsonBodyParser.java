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

package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.server.exception.DeserializationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

import static com.appsflyer.donkey.server.ring.handler.RingRequestField.BODY;

public class JsonBodyParser implements RingMiddleware {
  
  private final ObjectMapper mapper;
  
  public JsonBodyParser(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  @SuppressWarnings("OverlyBroadCatchBlock")
  @Override
  public IPersistentMap handle(IPersistentMap request) {
    Objects.requireNonNull(request, "Request map cannot be null");
    var body = (byte[]) request.valAt(BODY.keyword(), null);
    if (body == null) {
      return request;
    }
    try {
      var entity = mapper.readValue(body, Object.class);
      return request.assoc(BODY.keyword(), entity);
    } catch (IOException e) {
      throw new DeserializationException(e.getMessage(), e);
    }
  }
}
