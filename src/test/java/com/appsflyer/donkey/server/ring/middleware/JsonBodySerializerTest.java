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
import clojure.lang.RT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.appsflyer.donkey.server.ring.handler.RingResponseField.BODY;
import static org.junit.jupiter.api.Assertions.*;

@Tag("slow")
class JsonBodySerializerTest {
  
  private static RingMiddleware middleware;
  
  @BeforeAll
  static void beforeAll() {
    middleware = JsonBodySerializer.create(new ObjectMapper());
  }
  
  @Test
  void testNullResponse() {
    assertThrows(NullPointerException.class, () -> middleware.handle(null));
  }
  
  @Test
  void testEmptyResponse() {
    assertEquals(RT.map(), middleware.handle(RT.map()));
  }
  
  @Test
  void testEmptyBody() {
    IPersistentMap response = RT.map(BODY.keyword(), RT.map());
    assertArrayEquals("{}".getBytes(),
                      (byte[])middleware.handle(response).valAt(BODY.keyword()));
  }
  
  @Test
  void testBodyIsNull() {
    IPersistentMap response = RT.map(BODY.keyword(), null);
    assertEquals(response, middleware.handle(response));
  }
  
  @Test
  void testValidJSON() {
    IPersistentMap response = RT.map(BODY.keyword(),
                                     RT.map("foo", "bar", "fizz", "baz"));
    assertArrayEquals("{\"foo\":\"bar\",\"fizz\":\"baz\"}".getBytes(),
                      (byte[])middleware.handle(response).valAt(BODY.keyword()));
  }
}
