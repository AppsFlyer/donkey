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

package com.appsflyer.donkey.util;

import clojure.lang.*;
import com.appsflyer.donkey.client.exception.UnsupportedDataTypeException;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.multipart.MultipartForm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class TypeConverter {
  
  private static final Function<String, String> STRING_IDENTITY = Function.identity();
  
  private TypeConverter() {}
  
  /**
   * @return Comma separated string of all the elements in the iterable
   */
  public static String stringJoiner(Iterable<String> v) {
    return String.join(",", v);
  }
  
  public static IPersistentVector toVector(Collection<?> v) {
    return LazilyPersistentVector.createOwning(v.toArray());
  }
  
  public static IPersistentMap toPersistentMap(MultiMap entries) {
    return toPersistentMap(entries, TypeConverter::toVector);
  }
  
  public static IPersistentMap toPersistentMap(
      MultiMap entries,
      Function<List<String>, Object> aggregator) {
    
    return toPersistentMap(entries, STRING_IDENTITY, aggregator);
  }
  
  public static IPersistentMap toPersistentMap(
      MultiMap entries,
      Function<String, String> keyTransformer,
      Function<List<String>, Object> aggregator) {
    
    Set<String> names = entries.names();
    Object[] entriesArray = new Object[(names.size() * 2)];
    int i = 0;
    for (String name : names) {
      entriesArray[i] = keyTransformer.apply(name);
      List<String> entryList = entries.getAll(name);
      if (entryList.size() == 1) {
        entriesArray[i + 1] = entryList.get(0);
      } else {
        entriesArray[i + 1] = aggregator.apply(entryList);
      }
      i += 2;
    }
    return RT.mapUniqueKeys(entriesArray);
  }
  
  public static IPersistentMap toUrlDecodedPersistentMap(MultiMap entries) {
    Set<String> names = entries.names();
    Object[] entriesArray = new Object[(names.size() * 2)];
    int i = 0;
    for (String name : names) {
      entriesArray[i] = QueryStringDecoder.decodeComponent(name);
      entriesArray[i + 1] = QueryStringDecoder.decodeComponent(entries.get(name));
      i += 2;
    }
    return RT.mapUniqueKeys(entriesArray);
  }
  
  public static IPersistentMap toPersistentMap(Object[] values) {
    return RT.mapUniqueKeys(values);
  }
  
  /**
   * @param obj An object that can be converted to bytes
   * @return byte[] representation of the argument
   * @throws UnsupportedDataTypeException When argument type cannot be converted to bytes
   */
  static byte[] toBytes(Object obj) {
    if (obj instanceof byte[]) {
      return (byte[]) obj;
    }
    if (obj instanceof String) {
      return ((String) obj).getBytes(StandardCharsets.UTF_8);
    }
    if (obj instanceof InputStream) {
      try {
        return ((InputStream) obj).readAllBytes();
      } catch (IOException e) {
        throw new RuntimeException("Exception caught while consuming input stream", e);
      }
    }
    
    throw new UnsupportedDataTypeException(String.format(
        "Cannot create a byte[] from %s. Only String and InputStream are supported.",
        obj.getClass().getCanonicalName()));
  }
  
  public static Buffer toBuffer(Object obj) {
    return Buffer.buffer(toBytes(obj));
  }
  
  public static MultiMap toMultiMap(IPersistentMap map) {
    Objects.requireNonNull(map, "Cannot convert a null map to MultiMap");
    MultiMap res = MultiMap.caseInsensitiveMultiMap();
    for (var obj : map) {
      var entry = (IMapEntry) obj;
      res.add((String) entry.key(), (String) entry.val());
    }
    return res;
  }
  
  public static MultipartForm toMultipartForm(IPersistentMap map) {
    return MultipartFormConverter.from(map);
  }
}
