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
