package com.appsflyer.donkey.middleware;

import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.appsflyer.donkey.route.handler.ring.RingRequestField.QUERY_PARAMS;

class KeywordizeQueryParamsTest {
  
  @Test
  void testNullRequest() {
    assertThrows(NullPointerException.class, () -> MiddlewareProvider.keywordizeQueryParams(null));
  }
  
  @Test
  void testEmptyRequest() {
    assertEquals(RT.map(), MiddlewareProvider.keywordizeQueryParams(RT.map()));
  }
  
  @Test
  void testEmptyQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map());
    assertEquals(request, MiddlewareProvider.keywordizeQueryParams(request));
  }
  
  @Test
  void testQueryParamsValueIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), null);
    assertEquals(request, MiddlewareProvider.keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyIsAlreadyKeyword() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, "foo"), "bar"));
    
    assertThrows(ClassCastException.class, () -> MiddlewareProvider.keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(null, "foo"));
    assertThrows(NullPointerException.class, () -> MiddlewareProvider.keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyStartsWithColon() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(":foo", "bar"));
    IPersistentMap expected = RT.map(QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, ":foo"), "bar"));
    
    assertEquals(expected, MiddlewareProvider.keywordizeQueryParams(request));
  }
  
  @Test
  void testValidQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(),
                                    RT.map("foo", "bar", "fizz", "baz"));
    IPersistentMap expected = RT.map(
        QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, "foo"), "bar",
                                       RT.keyword(null, "fizz"), "baz"));
    
    assertEquals(expected, MiddlewareProvider.keywordizeQueryParams(request));
  }
}
