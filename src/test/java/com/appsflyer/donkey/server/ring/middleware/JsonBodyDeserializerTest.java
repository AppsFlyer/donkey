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
import com.appsflyer.donkey.server.exception.DeserializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.appsflyer.donkey.server.ring.handler.RingResponseField.BODY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow")
class JsonBodyDeserializerTest {
  
  private static RingMiddleware middleware;
  
  @BeforeAll
  static void beforeAll() {
    middleware = JsonBodyDeserializer.create(new ObjectMapper());
  }
  
  @Test
  void testNullRequest() {
    assertThrows(NullPointerException.class, () -> middleware.handle(null));
  }
  
  @Test
  void testEmptyRequest() {
    assertEquals(RT.map(), middleware.handle(RT.map()));
  }
  
  @Test
  void testEmptyBody() {
    IPersistentMap request = RT.map(BODY.keyword(), "{}".getBytes());
    assertEquals(RT.map(), middleware.handle(request).valAt(BODY.keyword()));
  }
  
  @Test
  void testBodyIsNull() {
    IPersistentMap request = RT.map(BODY.keyword(), null);
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testInvalidJSON() {
    IPersistentMap request1 = RT.map(BODY.keyword(), "invalid json");
    assertThrows(ClassCastException.class, () -> middleware.handle(request1));
    
    IPersistentMap request2 = RT.map(BODY.keyword(), "invalid json".getBytes());
    DeserializationException ex =
        assertThrows(DeserializationException.class, () -> middleware.handle(request2));
    assertEquals(400, ex.code());
  }
  
  
  @Test
  void testValidJSON() {
    IPersistentMap request1 = RT.map(BODY.keyword(),
                                     "{\"foo\":\"bar\",\"fizz\":\"baz\"}".getBytes());
    assertEquals(RT.map("foo", "bar", "fizz", "baz"),
                 middleware.handle(request1).valAt(BODY.keyword()));
    
    IPersistentMap request2 = RT.map(BODY.keyword(),
                                     "[\"foo\",\"bar\",\"fizz\",\"baz\"]".getBytes());
    assertEquals(RT.vector("foo", "bar", "fizz", "baz"),
                 middleware.handle(request2).valAt(BODY.keyword()));
  }
}
