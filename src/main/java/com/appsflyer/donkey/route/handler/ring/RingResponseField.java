package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.ValueExtractor;

import static com.appsflyer.donkey.util.TypeConverter.toBytes;

/**
 * The Enum class encapsulates the logic of extracting data from a Ring response.
 * Each element corresponds to a Ring response field. It implements getting the field's
 * name as a {@link Keyword}, and extracting the corresponding value from
 * the response.
 */
public enum RingResponseField implements ValueExtractor<IPersistentMap> {
  
  STATUS("status") {
    @Override
    public Integer from(IPersistentMap res) {
      return ((Number) res.valAt(keyword(), 200)).intValue();
    }
  },
  HEADERS("headers") {
    @Override
    public IPersistentMap from(IPersistentMap res) {
      return (IPersistentMap) res.valAt(keyword(), null);
    }
  },
  BODY("body") {
    @Override
    public byte[] from(IPersistentMap res) {
      var body = res.valAt(keyword(), null);
      if (body != null) {
        return toBytes(body);
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
