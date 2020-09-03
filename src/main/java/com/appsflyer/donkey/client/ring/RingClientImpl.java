package com.appsflyer.donkey.client.ring;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.client.ClientConfig;
import com.appsflyer.donkey.client.exception.UnsupportedDataTypeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.appsflyer.donkey.client.ring.RingRequestField.*;

public class RingClientImpl implements RingClient {
  
  private static final Logger logger = LoggerFactory.getLogger(RingClientImpl.class.getName());
  private final WebClient client;
  
  RingClientImpl(ClientConfig config) {
    client = WebClient.create(config.vertx(), config.clientOptions());
  }
  
  @Override
  public void request(IPersistentMap opts) {
    var method = (HttpMethod) METHOD.from(opts);
    Objects.requireNonNull(method, "HTTP request method is missing");
    
    //It's OK to suppress the warning here because a ClassCastException
    //would have been thrown during the call to `HANDLER`.
    @SuppressWarnings("unchecked")
    var handler =
        (Handler<AsyncResult<IPersistentMap>>) HANDLER.from(opts);
    Objects.requireNonNull(handler, "HTTP request handler is missing");
    
    Promise<IPersistentMap> promise = Promise.promise();
    promise.future().onComplete(handler);
    
    HttpRequest<Buffer> request = client.request(method, (String) URI.from(opts));
    
    try {
      addPort(request, opts);
      addHost(request, opts);
      addQueryParams(request, opts);
      addHeaders(request, opts);
      addBasicAuth(request, opts);
      addBearerToken(request, opts);
      addTimeout(request, opts);
    } catch (RuntimeException ex) {
      logger.error("Client request processing failed", ex);
      promise.fail(ex);
      return;
    }
    
    Buffer body;
    try {
      body = (Buffer) BODY.from(opts);
    } catch (UnsupportedDataTypeException ex) {
      logger.error(ex.getMessage());
      promise.fail(ex);
      return;
    }
    
    if (body == null) {
      request.send(new RingResponseAdapter(promise));
    } else {
      request.sendBuffer(body, new RingResponseAdapter(promise));
    }
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
  
  private void addTimeout(HttpRequest<Buffer> request, IPersistentMap opts) {
    var timeout = (Long) TIMEOUT.from(opts);
    if (timeout != null) {
      request.timeout(timeout);
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
