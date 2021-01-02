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

import com.appsflyer.donkey.client.exception.UnsupportedDataTypeException;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.appsflyer.donkey.util.TypeConverter.toBuffer;
import static com.appsflyer.donkey.util.TypeConverter.toBytes;
import static org.junit.jupiter.api.Assertions.*;

class TypeConverterTest {
  
  private static final String stringValue = "hello, world!";
  private static final byte[] byteValue = stringValue.getBytes(StandardCharsets.UTF_8);
  private static final Buffer bufferValue = Buffer.buffer(byteValue);
  
  @Test
  void testToBytesIdentity() {
    assertSame(byteValue, toBytes(byteValue));
  }
  
  @Test
  void testStringToBytes() {
    assertArrayEquals(byteValue, toBytes(stringValue));
  }
  
  @Test
  void testInputStreamToBytes() {
    assertArrayEquals(byteValue,
                      toBytes(new ByteArrayInputStream(byteValue)));
  }
  
  @Test
  void testBytesToBuffer() {
    assertEquals(bufferValue, toBuffer(byteValue));
  }
  
  @Test
  void testStringToBuffer() {
    assertEquals(bufferValue, toBuffer(stringValue));
  }
  
  @Test
  void testInputStreamToBuffer() {
    assertEquals(bufferValue, toBuffer(new ByteArrayInputStream(byteValue)));
  }
  
  @Test
  void testUnsupportedDataTypeException() {
    assertThrows(UnsupportedDataTypeException.class, () -> toBytes(new Object()));
    assertThrows(UnsupportedDataTypeException.class, () -> toBuffer(new Object()));
  }
}
