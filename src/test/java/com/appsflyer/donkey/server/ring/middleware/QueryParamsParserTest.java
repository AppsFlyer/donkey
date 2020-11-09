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
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryParamsParserTest {
  
  private static RingMiddleware middleware;
  private static final Keyword QUERY_STRING = Keyword.intern("query-string");
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  
  @BeforeAll
  static void beforeAll() {
    middleware = new QueryParamsParser();
  }
  
  private static String encode(String val) throws UnsupportedEncodingException {
    return URLEncoder.encode(val, StandardCharsets.UTF_8.toString());
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
  void testEmptyQueryString() {
    IPersistentMap request = RT.map(QUERY_STRING, "");
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testQueryParamsExist() {
    IPersistentMap request = RT.map(QUERY_PARAMS, null);
    assertEquals(request, middleware.handle(request));
    
    request = RT.map(QUERY_PARAMS, "");
    assertEquals(request, middleware.handle(request));
    
    request = RT.map(QUERY_PARAMS, RT.map());
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testQueryStringValueIsNull() {
    IPersistentMap request = RT.map(QUERY_STRING, null);
    assertEquals(request, middleware.handle(request));
  }
  
  @Test
  void testKeyIsEmptyString() {
    IPersistentMap request = RT.map(QUERY_STRING, " =foo");
    IPersistentMap expected = request.assoc(QUERY_PARAMS, RT.map(" ", "foo"));
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testSimpleQueryParams() {
    IPersistentMap request = RT.map(QUERY_STRING, "foo=bar&fizz=baz");
    IPersistentMap expected =
        request.assoc(QUERY_PARAMS, RT.map("foo", "bar", "fizz", "baz"));
    
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testRepeatingKeyQueryParams() {
    IPersistentMap request = RT.map(QUERY_STRING, "foo=bar&foo=baz&foo=fizz");
    IPersistentMap expected =
        request.assoc(QUERY_PARAMS, RT.map("foo", RT.vector("bar", "baz", "fizz")));
    
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testUrlEncodedQueryParams1() throws UnsupportedEncodingException {
    //"f o o=B{a}r&fizz%=^b  az"
    IPersistentMap request = RT.map(
        QUERY_STRING, "city=" + encode("New York") + "&" +
            encode("first name") + "=" + "John" + "&" +
            encode("last name") + "=" + "Baker");
    
    IPersistentMap expected = request.assoc(QUERY_PARAMS,
                                            RT.map("city", "New York",
                                                   "first name", "John",
                                                   "last name", "Baker"));
    
    assertEquals(expected, middleware.handle(request));
  }
  
  @Test
  void testUrlEncodedQueryParams2() throws UnsupportedEncodingException {
    IPersistentMap request = RT.map(
        QUERY_STRING,
        encode("f o o") + "=" + encode("B{a}r") + "&" +
            encode("fizz%") + "=" + "^b  az");
    
    IPersistentMap expected = request.assoc(QUERY_PARAMS,
                                            RT.map("f o o", "B{a}r",
                                                   "fizz%", "^b  az"));
    
    assertEquals(expected, middleware.handle(request));
  }
}
