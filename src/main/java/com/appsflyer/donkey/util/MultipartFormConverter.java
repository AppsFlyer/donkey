package com.appsflyer.donkey.util;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.Objects;

public final class MultipartFormConverter {
  
  private MultipartFormConverter() {}
  
  public static MultipartForm from(IPersistentMap map) {
    Objects.requireNonNull(map, "Cannot convert a null map to MultipartForm");
    MultipartForm res = MultipartForm.create();
    for (var obj : map) {
      var entry = (IMapEntry) obj;
      var val = entry.val();
      Objects.requireNonNull(val, "Form attribute value cannot be null");
      
      if (val instanceof String) {
        res.attribute((String) entry.key(), (String) val);
      } else {
        addFile(res, (String) entry.key(), (IPersistentMap) val);
      }
    }
    return res;
  }
  
  private static void addFile(MultipartForm form, String key, IPersistentMap fileOpts) {
    var filename = (String) fileOpts.valAt("filename");
    var pathname = (String) fileOpts.valAt("pathname");
    var mediaType = (String) fileOpts.valAt("media-type");
    var uploadAs = (String) fileOpts.valAt("upload-as", "");
    if ("text".equals(uploadAs)) {
      form.textFileUpload(key, filename, pathname, mediaType);
    } else {
      form.binaryFileUpload(key, filename, pathname, mediaType);
    }
  }
}
