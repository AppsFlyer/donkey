package com.appsflyer.donkey.client.ring;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.ClientConfig;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.Objects;

import static com.appsflyer.donkey.client.ring.RingRequestField.*;
import static com.appsflyer.donkey.util.TypeConverter.*;

public class RingClientImpl implements RingClient {
  
  private final WebClient client;
  
  RingClientImpl(ClientConfig config) {
    client = WebClient.create(config.vertx(), config.clientOptions());
  }
  
  @Override
  public HttpRequest<Buffer> request(IPersistentMap opts) {
    var method = (HttpMethod) METHOD.from(opts);
    Objects.requireNonNull(method, "HTTP request method is missing");
    
    HttpRequest<Buffer> request = client.request(method, (String) URI.from(opts));
    
    addPort(request, opts);
    addHost(request, opts);
    addSsl(request, opts);
    addQueryParams(request, opts);
    addHeaders(request, opts);
    addBasicAuth(request, opts);
    addBearerToken(request, opts);
    addTimeout(request, opts);
    
    return request;
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
  
  private void addPort(HttpRequest<Buffer> request, IPersistentMap opts) {
    var port = (Integer) PORT.from(opts);
    if (port != null) {
      request.port(port);
    }
  }
  
  private void addHost(HttpRequest<Buffer> request, IPersistentMap opts) {
    var host = (String) HOST.from(opts);
    if (host != null) {
      request.host(host);
    }
  }
  
  private void addSsl(HttpRequest<Buffer> request, IPersistentMap opts) {
    var ssl = (Boolean) SSL.from(opts);
    if (ssl != null) {
      request.ssl(ssl);
    }
  }
  
  private void addTimeout(HttpRequest<Buffer> request, IPersistentMap opts) {
    var timeout = (Long) TIMEOUT.from(opts);
    if (timeout != null) {
      request.timeout(timeout * 1000);
    }
  }
  
  private void addBearerToken(HttpRequest<Buffer> request, IPersistentMap opts) {
    var token = (String) BEARER_TOKEN.from(opts);
    if (token != null) {
      request.bearerTokenAuthentication(token);
    }
  }
  
  private void addBasicAuth(HttpRequest<Buffer> request, IPersistentMap opts) {
    var credentials = (IPersistentMap) BASIC_AUTH.from(opts);
    if (credentials != null) {
      request.basicAuthentication(
          (String) credentials.valAt("id"),
          (String) credentials.valAt("password"));
    }
  }
  
  private void addQueryParams(HttpRequest<Buffer> request, IPersistentMap opts) {
    var params = (IPersistentMap) QUERY_PARAMS.from(opts);
    if (params != null) {
      for (var obj : params) {
        var entry = (IMapEntry) obj;
        request.addQueryParam((String) entry.getKey(), (String) entry.getValue());
      }
    }
  }
  
  private void addHeaders(HttpRequest<Buffer> request, IPersistentMap opts) {
    var headers = (IPersistentMap) HEADERS.from(opts);
    if (headers != null) {
      for (var obj : headers) {
        var entry = (IMapEntry) obj;
        request.putHeader((String) entry.getKey(), (String) entry.getValue());
      }
    }
  }
}
