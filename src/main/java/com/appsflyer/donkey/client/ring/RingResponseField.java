package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.ValueExtractor;
import com.appsflyer.donkey.util.TypeConverter;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import org.jetbrains.annotations.Nullable;

import static com.appsflyer.donkey.util.TypeConverter.toPersistentMap;

/**
 * The Enum class encapsulates the logic of translating between a Vertx {@link HttpResponse}
 * and a Ring response.
 * Each element corresponds to a Ring response field. It implements getting the field's
 * name as a {@link Keyword} or string, and extracting the corresponding value from
 * the {@link HttpResponse}.
 */
public enum RingResponseField implements ValueExtractor<HttpResponse<Buffer>> {
  
  STATUS("status") {
    @Override
    public Integer from(HttpResponse<Buffer> res) {
      return res.statusCode();
    }
  },
  HEADERS("headers") {
    @Nullable
    @Override
    public IPersistentMap from(HttpResponse<Buffer> res) {
      MultiMap headers = res.headers();
      if (headers.isEmpty()) {
        return null;
      }
      return toPersistentMap(headers, TypeConverter::stringJoiner);
    }
  },
  BODY("body") {
    @Override
    public byte[] from(HttpResponse<Buffer> res) {
      Buffer body = res.body();
      if (body != null) {
        return body.getBytes();
      }
      return BYTES;
    }
  };
  
  private static final byte[] BYTES = new byte[0];
  private final Keyword keyword;
  
  RingResponseField(String field) {
    keyword = Keyword.intern(field);
  }
  
  /**
   * @return The field name as a Keyword
   */
  public Keyword keyword() {
    return keyword;
  }
}
