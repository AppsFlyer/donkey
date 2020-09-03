package com.appsflyer.donkey.route.ring.middleware;

import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import org.junit.jupiter.api.Test;

import static com.appsflyer.donkey.route.handler.ring.RingRequestField.QUERY_PARAMS;
import static com.appsflyer.donkey.route.ring.middleware.MiddlewareProvider.keywordizeQueryParams;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeywordizeQueryParamsTest {
  
  @Test
  void testNullRequest() {
    assertThrows(NullPointerException.class, () -> keywordizeQueryParams(null));
  }
  
  @Test
  void testEmptyRequest() {
    assertEquals(RT.map(), keywordizeQueryParams(RT.map()));
  }
  
  @Test
  void testEmptyQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map());
    assertEquals(request, keywordizeQueryParams(request));
  }
  
  @Test
  void testQueryParamsValueIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), null);
    assertEquals(request, keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyIsAlreadyKeyword() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, "foo"), "bar"));
  
    assertThrows(ClassCastException.class, () -> keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyIsNull() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(null, "foo"));
    assertThrows(NullPointerException.class, () -> keywordizeQueryParams(request));
  }
  
  @Test
  void testKeyStartsWithColon() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(), RT.map(":foo", "bar"));
    IPersistentMap expected = RT.map(QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, ":foo"), "bar"));
  
    assertEquals(expected, keywordizeQueryParams(request));
  }
  
  @Test
  void testValidQueryParams() {
    IPersistentMap request = RT.map(QUERY_PARAMS.keyword(),
                                    RT.map("foo", "bar", "fizz", "baz"));
    IPersistentMap expected = RT.map(
        QUERY_PARAMS.keyword(), RT.map(RT.keyword(null, "foo"), "bar",
                                       RT.keyword(null, "fizz"), "baz"));
  
    assertEquals(expected, keywordizeQueryParams(request));
  }
}
