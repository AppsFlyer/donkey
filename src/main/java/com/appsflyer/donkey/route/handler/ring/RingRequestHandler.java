package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.*;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.appsflyer.donkey.route.handler.ring.Constants.RING_REQUEST_FIELD;
import static io.vertx.core.http.HttpMethod.*;
import static io.vertx.core.http.HttpVersion.*;

public class RingRequestHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(RingRequestHandler.class.getName());
  
  private static final Keyword SERVER_PORT = Keyword.intern("server-port");
  private static final Keyword SERVER_NAME = Keyword.intern("server-name");
  private static final Keyword REMOTE_ADDRESS = Keyword.intern("remote-addr");
  private static final Keyword URI = Keyword.intern("uri");
  private static final Keyword QUERY_STRING = Keyword.intern("query-string");
  private static final Keyword QUERY_PARAMS = Keyword.intern("query-params");
  private static final Keyword PATH_PARAMS = Keyword.intern("path-params");
  private static final Keyword FORM_PARAMS = Keyword.intern("form-params");
  private static final Keyword SCHEME = Keyword.intern("scheme");
  private static final Keyword REQUEST_METHOD = Keyword.intern("request-method");
  private static final Keyword PROTOCOL = Keyword.intern("protocol");
  private static final Keyword SSL_CLIENT_CERT = Keyword.intern("ssl-client-cert");
  private static final Keyword HEADERS = Keyword.intern("headers");
  private static final Keyword BODY = Keyword.intern("body");
  
  private static final Map<String, Keyword> schemeMapping =
      Map.of("http", Keyword.intern("http"),
             "https", Keyword.intern("https"));
  
  private static final Map<HttpMethod, Keyword> methodMapping =
      Map.of(GET, Keyword.intern("get"),
             POST, Keyword.intern("post"),
             PUT, Keyword.intern("put"),
             DELETE, Keyword.intern("delete"),
             PATCH, Keyword.intern("patch"),
             HEAD, Keyword.intern("head"),
             OPTIONS, Keyword.intern("options"),
             TRACE, Keyword.intern("trace"),
             CONNECT, Keyword.intern("connect"));
  
  private static final Map<HttpVersion, String> protocolMapping =
      Map.of(HTTP_1_0, "HTTP/1.0",
             HTTP_1_1, "HTTP/1.1",
             HTTP_2, "HTTP/2");
  
  private static String stringJoiner(List<String> v)
  {
    return String.join(",", v);
  }
  
  private static IPersistentVector toVector(List<?> v)
  {
    return LazilyPersistentVector.createOwning(v.toArray());
  }
  
  private static IPersistentMap toPersistentMap(MultiMap entries)
  {
    return toPersistentMap(entries, RingRequestHandler::toVector);
  }
  
  private static IPersistentMap toPersistentMap(
      MultiMap entries,
      Function<List<String>, Object> aggregator)
  {
    Object[] entriesArray = new Object[(entries.size() << 1)];
    int i = 0;
    for (String name : entries.names()) {
      entriesArray[i] = name;
      List<String> entryList = entries.getAll(name);
      if (entryList.size() == 1) {
        entriesArray[i + 1] = entryList.get(0);
      }
      else {
        entriesArray[i + 1] = aggregator.apply(entryList);
      }
      i += 2;
    }
    return new PersistentArrayMap(entriesArray);
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    List<Object> values = new ArrayList<>(14 << 1);
    
    addServerPort(ctx, values)
        .addServerName(ctx, values)
        .addProtocol(ctx, values)
        .addScheme(ctx, values)
        .addSslClientCert(ctx, values)
        .addRemoteAddress(ctx, values)
        .addMethod(ctx, values)
        .addHeaders(ctx, values)
        .addUri(ctx, values)
        .addQueryString(ctx, values)
        .addQueryParams(ctx, values)
        .addPathParams(ctx, values)
        .addFormParams(ctx, values)
        .addBody(ctx, values);
    
    ctx.put(RING_REQUEST_FIELD, new PersistentArrayMap(values.toArray()));
    ctx.next();
  }
  
  private RingRequestHandler addServerPort(RoutingContext ctx, List<Object> values)
  {
    SocketAddress localAddress = ctx.request().localAddress();
    if (localAddress != null) {
      addNameValuePair(SERVER_PORT, localAddress.port(), values);
    }
    return this;
  }
  
  private RingRequestHandler addServerName(RoutingContext ctx, List<Object> values)
  {
    String host = ctx.request().host();
    if (host != null) {
      return addNameValuePair(SERVER_NAME, host, values);
    }
    
    SocketAddress localAddress = ctx.request().localAddress();
    if (localAddress != null) {
      addNameValuePair(SERVER_NAME, localAddress.host(), values);
    }
    
    return this;
  }
  
  private RingRequestHandler addRemoteAddress(RoutingContext ctx, List<Object> values)
  {
    var forwardedFor = ctx.request().getHeader("x-forwarded-for");
    if (forwardedFor != null) {
      return addNameValuePair(REMOTE_ADDRESS, forwardedFor, values);
    }
    
    SocketAddress remoteAddress = ctx.request().remoteAddress();
    if (remoteAddress != null) {
      addNameValuePair(REMOTE_ADDRESS, remoteAddress.port(), values);
    }
    
    return this;
  }
  
  private RingRequestHandler addUri(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(URI, ctx.request().uri(), values);
  }
  
  private RingRequestHandler addScheme(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(SCHEME, schemeMapping.get(ctx.request().scheme()), values);
  }
  
  private RingRequestHandler addMethod(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(
        REQUEST_METHOD,
        methodMapping.get(ctx.request().method()),
        values);
  }
  
  private RingRequestHandler addProtocol(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(
        PROTOCOL,
        protocolMapping.get(ctx.request().version()),
        values);
  }
  
  private RingRequestHandler addSslClientCert(RoutingContext ctx, List<Object> values)
  {
    if (ctx.request().isSSL()) {
      try {
        Certificate[] certificates = ctx.request().sslSession().getPeerCertificates();
        return addNameValuePair(SSL_CLIENT_CERT, certificates, values);
      } catch (SSLPeerUnverifiedException e) {
        logger.debug("Caught exception getting SSL peer certificates: {}", e.getMessage());
      }
    }
    return this;
  }
  
  private RingRequestHandler addBody(RoutingContext ctx, List<Object> values)
  {
    Buffer body = ctx.getBody();
    if (body != null) {
      addNameValuePair(BODY, body.getBytes(), values);
    }
    return this;
  }
  
  private RingRequestHandler addHeaders(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(
        HEADERS,
        toPersistentMap(ctx.request().headers(), RingRequestHandler::stringJoiner),
        values);
  }
  
  private RingRequestHandler addQueryString(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(QUERY_STRING, ctx.request().query(), values);
  }
  
  private RingRequestHandler addQueryParams(RoutingContext ctx, List<Object> values)
  {
    MultiMap queryParams;
    try {
      queryParams = ctx.queryParams();
    } catch (HttpStatusException ex) {
      logger.warn("{}. Raw query string: {}", ex.getMessage(), ctx.request().query());
      return this;
    }
    
    return addNameValuePair(QUERY_PARAMS, toPersistentMap(queryParams), values);
  }
  
  private RingRequestHandler addPathParams(RoutingContext ctx, List<Object> values)
  {
    Map<String, String> pathParams = ctx.pathParams();
    
    if (pathParams.isEmpty()) {
      return this;
    }
    Object[] pathParamsArray = new Object[(pathParams.size() << 1)];
    int i = 0;
    for (Map.Entry<String, String> entry : pathParams.entrySet()) {
      pathParamsArray[i] = entry.getKey();
      pathParamsArray[i + 1] = entry.getValue();
      i += 2;
    }
    return addNameValuePair(PATH_PARAMS, new PersistentArrayMap(pathParamsArray), values);
  }
  
  private RingRequestHandler addFormParams(RoutingContext ctx, List<Object> values)
  {
    if (!ctx.request().isExpectMultipart()) {
      return this;
    }
    
    MultiMap formAttributes = ctx.request().formAttributes();
    if (formAttributes.isEmpty()) {
      return this;
    }
    return addNameValuePair(FORM_PARAMS, toPersistentMap(formAttributes), values);
  }
  
  private RingRequestHandler addNameValuePair(Keyword name, Object value, List<Object> values)
  {
    values.add(name);
    values.add(value);
    return this;
  }
}
