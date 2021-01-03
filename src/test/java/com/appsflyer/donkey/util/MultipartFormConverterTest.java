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

import clojure.lang.RT;
import io.vertx.ext.web.multipart.MultipartForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultipartFormConverterTest {
  
  @Test
  void createWithAttributes() {
    var attributes = RT.map("name", "John", "city", "New York");
    MultipartForm form = TypeConverter.toMultipartForm(attributes);
    form.forEach(v -> {
      assertTrue(v.isAttribute());
      assertEquals(attributes.valAt(v.name()), v.value());
    });
  }
  
  @Test
  void createWithTextFile() {
    var fileAttributes = RT.map("filename", "upload-text",
                                "pathname", "resources/upload-text.json",
                                "media-type", "application/json",
                                "upload-as", "text");
    var attributes = RT.map("file-data", fileAttributes);
    MultipartForm form = TypeConverter.toMultipartForm(attributes);
    form.forEach(v -> {
      assertTrue(v.isFileUpload());
      assertTrue(v.isText());
      assertEquals("file-data", v.name());
      assertEquals(v.filename(), fileAttributes.valAt("filename"));
      assertEquals(v.pathname(), fileAttributes.valAt("pathname"));
      assertEquals(v.mediaType(), fileAttributes.valAt("media-type"));
    });
  }
  
  @Test
  void createWithBinaryFile() {
    var fileAttributes = RT.map("filename", "upload-text",
                                "pathname", "resources/upload-text.json",
                                "media-type", "application/json",
                                "upload-as", "binary");
    var attributes = RT.map("file-data", fileAttributes);
    MultipartForm form = TypeConverter.toMultipartForm(attributes);
    form.forEach(v -> {
      assertTrue(v.isFileUpload());
      assertFalse(v.isText());
      assertEquals("file-data", v.name());
      assertEquals(v.filename(), fileAttributes.valAt("filename"));
      assertEquals(v.pathname(), fileAttributes.valAt("pathname"));
      assertEquals(v.mediaType(), fileAttributes.valAt("media-type"));
    });
  }
}
