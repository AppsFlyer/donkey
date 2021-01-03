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
import com.appsflyer.donkey.server.ring.middleware.FormParamsKeywordizer.Options;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow")
class KeywordizeFormParamsTest {
  
  private static RingMiddleware middleware;
  private static final Keyword FORM_PARAMS = Keyword.intern("form-params");
  
  @BeforeAll
  static void beforeAll() {
    middleware = FormParamsKeywordizer.create(new Options(true));
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
  void testEmptyFormParams() {
    IPersistentMap request = RT.map(FORM_PARAMS, RT.map());
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testFormParamsValueIsNull() {
    IPersistentMap request = RT.map(FORM_PARAMS, null);
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testKeyIsAlreadyKeyword() {
    IPersistentMap request = RT.map(FORM_PARAMS, RT.map(RT.keyword(null, "foo"), "bar"));
    
    assertThrows(ClassCastException.class, () -> middleware.handle(request));
  }
  
  @Test
  void testKeyIsNull() {
    IPersistentMap request = RT.map(FORM_PARAMS, RT.map(null, "foo"));
    assertThrows(NullPointerException.class, () -> middleware.handle(request));
  }
  
  @Test
  void testKeyStartsWithColon() {
    IPersistentMap request = RT.map(FORM_PARAMS, RT.map(":foo", "bar"));
    IPersistentMap expected = RT.map(FORM_PARAMS, RT.map(RT.keyword(null, ":foo"), "bar"));
    
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testValidFormParams() {
    IPersistentMap request = RT.map(FORM_PARAMS,
                                    RT.map("foo", "bar", "fizz", "baz"));
    IPersistentMap expected = RT.map(
        FORM_PARAMS, RT.map(RT.keyword(null, "foo"), "bar",
                            RT.keyword(null, "fizz"), "baz"));
    
    assertEquals(expected, middleware.handle(request));
  }
}
