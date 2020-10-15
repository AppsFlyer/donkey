package com.appsflyer.donkey;

import clojure.lang.Keyword;
import clojure.lang.Ratio;
import clojure.lang.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jsonista.jackson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility class for serializing and deserializing JSON into Clojure types.
 */
public final class ClojureObjectMapper {
  
  private ClojureObjectMapper() {}
  
  private static class ClojureObjectMapperHolder {
    
    private static final ObjectMapper mapper;
    
    static {
      var clojureModule = new SimpleModule("Clojure")
          .addDeserializer(List.class, new PersistentVectorDeserializer())
          .addDeserializer(Map.class, new PersistentHashMapDeserializer())
          .addSerializer(Keyword.class, new KeywordSerializer(false))
          .addSerializer(Ratio.class, new RatioSerializer())
          .addSerializer(Symbol.class, new SymbolSerializer())
          .addSerializer(Date.class, new DateSerializer())
          .addKeySerializer(Keyword.class, new KeywordSerializer(true));
      
      mapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .registerModule(clojureModule)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
  }
  
  public static ObjectMapper mapper() {
    return ClojureObjectMapperHolder.mapper;
  }
  
  public static byte[] serialize(Object val) {
    try {
      return ClojureObjectMapperHolder.mapper.writeValueAsBytes(val);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static Object deserialize(String val) {
    return deserialize(val.getBytes(StandardCharsets.UTF_8));
  }
  
  public static Object deserialize(byte[] val) {
    try {
      return ClojureObjectMapperHolder.mapper.readValue(val, Object.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
