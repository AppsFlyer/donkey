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

@Tag("Slow")
class JsonBodyDeserializerTest {
  
  private static RingMiddleware middleware;
  
  @BeforeAll
  static void beforeAll() {
    middleware = new JsonBodyDeserializer(new ObjectMapper());
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
