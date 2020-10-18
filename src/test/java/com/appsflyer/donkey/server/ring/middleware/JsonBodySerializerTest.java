package com.appsflyer.donkey.server.ring.middleware;

import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import static com.appsflyer.donkey.server.ring.handler.RingResponseField.BODY;
import static org.junit.jupiter.api.Assertions.*;

@Tag("Slow")
class JsonBodySerializerTest {
  
  private static RingMiddleware middleware;
  
  @BeforeAll
  static void beforeAll() {
    middleware = new JsonBodySerializer(new ObjectMapper());
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
