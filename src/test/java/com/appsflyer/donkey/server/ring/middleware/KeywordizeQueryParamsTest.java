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
import clojure.lang.Keyword;
import clojure.lang.RT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("Slow")
class KeywordizeQueryParamsTest {
  
  private static RingMiddleware middleware;
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  
  @BeforeAll
  static void beforeAll() {
    middleware = QueryParamsKeywordizer.getInstance();
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
  void testEmptyQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS, RT.map());
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testQueryParamsValueIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS, null);
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testKeyIsAlreadyKeyword() {
    IPersistentMap request = RT.map(QUERY_PARAMS, RT.map(RT.keyword(null, "foo"), "bar"));
    
    assertThrows(ClassCastException.class, () -> middleware.handle(request));
  }
  
  @Test
  void testKeyIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS, RT.map(null, "foo"));
    assertThrows(NullPointerException.class, () -> middleware.handle(request));
  }
  
  @Test
  void testKeyStartsWithColon() {
    IPersistentMap request = RT.map(QUERY_PARAMS, RT.map(":foo", "bar"));
    IPersistentMap expected = RT.map(QUERY_PARAMS, RT.map(RT.keyword(null, ":foo"), "bar"));
    
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testValidQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS,
                                    RT.map("foo", "bar", "fizz", "baz"));
    IPersistentMap expected = RT.map(
        QUERY_PARAMS, RT.map(RT.keyword(null, "foo"), "bar",
                             RT.keyword(null, "fizz"), "baz"));
    
    assertEquals(expected, middleware.handle(request));
  }
}
