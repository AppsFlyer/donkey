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

package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.server.exception.SerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

import static com.appsflyer.donkey.server.ring.handler.RingResponseField.BODY;

public final class JsonBodySerializer implements RingMiddleware {
  
  public static RingMiddleware create(ObjectMapper mapper) {
    return new JsonBodySerializer(mapper);
  }
  
  private final ObjectMapper mapper;
  
  private JsonBodySerializer(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  @SuppressWarnings("OverlyBroadCatchBlock")
  @Override
  public IPersistentMap handle(IPersistentMap response) {
    Objects.requireNonNull(response, "Response map cannot be null");
    var body = response.valAt(BODY.keyword(), null);
    if (body == null) {
      return response;
    }
    
    try {
      return response.assoc(BODY.keyword(), mapper.writeValueAsBytes(body));
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }
}
