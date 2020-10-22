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

@Tag("Slow")
class KeywordizeFormParamsTest {
  
  private static RingMiddleware middleware;
  private static final Keyword FORM_PARAMS = Keyword.intern("form-params");
  
  @BeforeAll
  static void beforeAll() {
    middleware = new FormParamsKeywordizer(new Options(true));
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
