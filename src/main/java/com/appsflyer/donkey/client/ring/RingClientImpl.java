package com.appsflyer.donkey.client.ring;

import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.ClientConfig;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;

import static com.appsflyer.donkey.util.TypeConverter.*;

public class RingClientImpl implements RingClient {
  
  private final WebClient client;
  private final RequestFactory requestFactory;
  
  RingClientImpl(ClientConfig config) {
    client = WebClient.create(config.vertx(), config.clientOptions());
    requestFactory = new RequestFactory(client);
  }
  
  @Override
  public HttpRequest<Buffer> request(IPersistentMap opts) {
    return requestFactory.create(opts);
  }
  
  @Override
  public Future<IPersistentMap> send(HttpRequest<Buffer> request) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.send(new RingResponseAdapter(promise));
    return promise.future();
  }
  
  @Override
  public Future<IPersistentMap> send(HttpRequest<Buffer> request, Object body) {
    Buffer buffer;
    try {
      buffer = toBuffer(body);
    } catch (Throwable ex) {
      Promise<IPersistentMap> promise = Promise.promise();
      promise.fail(ex);
      return promise.future();
    }
    return send(request, buffer);
  }
  
  @Override
  public Future<IPersistentMap> send(HttpRequest<Buffer> request, Buffer body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendBuffer(body, new RingResponseAdapter(promise));
    return promise.future();
  }
  
  @Override
  public Future<IPersistentMap> sendForm(HttpRequest<Buffer> request, IPersistentMap body) {
    return sendForm(request, toMultiMap(body));
  }
  
  @Override
  public Future<IPersistentMap> sendForm(HttpRequest<Buffer> request, MultiMap body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendForm(body, new RingResponseAdapter(promise));
    return promise.future();
  }
  
  @Override
  public Future<IPersistentMap> sendMultiPartForm(HttpRequest<Buffer> request, IPersistentMap body) {
    return sendMultiPartForm(request, toMultipartForm(body));
  }
  
  @Override
  public Future<IPersistentMap> sendMultiPartForm(HttpRequest<Buffer> request, MultipartForm body) {
    Promise<IPersistentMap> promise = Promise.promise();
    request.sendMultipartForm(body, new RingResponseAdapter(promise));
    return promise.future();
  }
  
  @Override
  public void shutdown() {
    client.close();
  }
  
}
