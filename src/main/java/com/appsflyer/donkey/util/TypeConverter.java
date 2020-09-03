package com.appsflyer.donkey.util;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.LazilyPersistentVector;
import clojure.lang.RT;
import com.appsflyer.donkey.client.exception.UnsupportedDataTypeException;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class TypeConverter {
  
  private TypeConverter() {}
  
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
    Object[] entriesArray = new Object[(entries.size() << 1)];
    int i = 0;
    for (String name : entries.names()) {
      entriesArray[i] = name;
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
  
  public static IPersistentMap toPersistentMap(Object[] values) {
    return RT.mapUniqueKeys(values);
  }
  
  /**
   * @param obj An object that can be converted to bytes
   * @return byte[] representation of the argument
   * @throws UnsupportedDataTypeException When argument type cannot be converted to bytes
   */
  public static byte[] toBytes(Object obj) {
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
        "Cannot create a byte[] from %s. Only byte[] and String are supported.",
        obj.getClass().getCanonicalName()));
  }
  
  public static Buffer toBuffer(Object obj) {
    return Buffer.buffer(toBytes(obj));
  }
}
