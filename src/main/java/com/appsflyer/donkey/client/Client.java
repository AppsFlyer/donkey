package com.appsflyer.donkey.client;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.multipart.MultipartForm;

public interface Client<T> {
  
  HttpRequest<Buffer> request(T opts);
  
  Future<T> send(HttpRequest<Buffer> request);
  
  Future<T> send(HttpRequest<Buffer> request, Buffer body);
  
  Future<T> sendForm(HttpRequest<Buffer> request, MultiMap body);
  
  Future<T> sendMultiPartForm(HttpRequest<Buffer> request, MultipartForm body);
  
  void shutdown();
}
