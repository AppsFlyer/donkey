package com.appsflyer.donkey.client.ring;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.Objects;

import static com.appsflyer.donkey.client.ring.ClojureRequestField.*;

/**
 * A factory that constructs an {@link HttpRequest} from a Clojure map.
 */
public class RequestFactory {
  
  private final WebClient client;
  
  RequestFactory(WebClient client) {
    this.client = client;
  }
  
  public HttpRequest<Buffer> create(IPersistentMap opts) {
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
        request.putHeader((String) entry.getKey(), entry.getValue().toString());
      }
    }
  }
}
