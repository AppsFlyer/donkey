package com.appsflyer.donkey.route.handler.ring;

import clojure.lang.*;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.appsflyer.donkey.route.handler.ring.Constants.LAST_HANDLER_RESPONSE_FIELD;
import static com.appsflyer.donkey.route.handler.ring.RingRequestField.*;

/**
 * Handler responsible for converting an {@link io.vertx.core.http.HttpServerRequest}
 * to a Ring compliant Clojure map.
 * <p></p>
 * See the Ring <a href="https://github.com/ring-clojure/ring/blob/master/SPEC">specification</a> for more details.
 */
public class RingRequestHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(RingRequestHandler.class.getName());
  
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
    ctx.put(LAST_HANDLER_RESPONSE_FIELD, getImmutableRequest(ctx)).next();
  }
  
  private IPersistentMap getMutableRequest(RoutingContext ctx)
  {
    return new MutableRingRequestMap(ctx);
  }
  
  public IPersistentMap getImmutableRequest(RoutingContext ctx)
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
    
    return PersistentHashMap.create(values.toArray());
  }
  
  private RingRequestHandler addServerPort(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(SERVER_PORT.keyword(), SERVER_PORT.get(ctx), values);
  }
  
  private RingRequestHandler addServerName(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(SERVER_NAME.keyword(), SERVER_NAME.get(ctx), values);
  }
  
  private RingRequestHandler addRemoteAddress(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(REMOTE_ADDRESS.keyword(), REMOTE_ADDRESS.get(ctx), values);
  }
  
  private RingRequestHandler addUri(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(URI.keyword(), URI.get(ctx), values);
  }
  
  private RingRequestHandler addScheme(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(SCHEME.keyword(), SCHEME.get(ctx), values);
  }
  
  private RingRequestHandler addMethod(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(REQUEST_METHOD.keyword(), REQUEST_METHOD.get(ctx), values);
  }
  
  private RingRequestHandler addProtocol(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(PROTOCOL.keyword(), PROTOCOL.get(ctx), values);
  }
  
  private RingRequestHandler addSslClientCert(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(CLIENT_CERT.keyword(), CLIENT_CERT.get(ctx), values);
  }
  
  private RingRequestHandler addBody(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(BODY.keyword(), BODY.get(ctx), values);
  }
  
  private RingRequestHandler addHeaders(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(
        HEADERS.keyword(),
        toPersistentMap(((ClojureMultiMapWrapper) HEADERS.get(ctx)).impl(),
                        RingRequestHandler::stringJoiner),
        values);
  }
  
  private RingRequestHandler addQueryString(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(QUERY_STRING.keyword(), QUERY_STRING.get(ctx), values);
  }
  
  private RingRequestHandler addQueryParams(RoutingContext ctx, List<Object> values)
  {
    return addNameValuePair(QUERY_PARAMS.keyword(), QUERY_PARAMS.get(ctx), values);
  }
  
  private RingRequestHandler addPathParams(RoutingContext ctx, List<Object> values)
  {
    Map<?, ?> pathParams = ((ClojureMutableHashMap<?, ?>) PATH_PARAMS.get(ctx)).impl();
  
    if (pathParams.isEmpty()) {
      return this;
    }
    Object[] pathParamsArray = new Object[(pathParams.size() << 1)];
    int i = 0;
    for (Object obj : pathParams.entrySet()) {
      pathParamsArray[i] = ((Map.Entry<?, ?>) obj).getKey();
      pathParamsArray[i + 1] = ((Map.Entry<?, ?>) obj).getValue();
      i += 2;
    }
    return addNameValuePair(PATH_PARAMS.keyword(), new PersistentArrayMap(pathParamsArray), values);
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
    return addNameValuePair(
        FORM_PARAMS.keyword(),
        toPersistentMap(((ClojureMultiMapWrapper) FORM_PARAMS.get(ctx)).impl()),
        values);
  }
  
  private RingRequestHandler addNameValuePair(Keyword name, Object value, List<Object> values)
  {
    if (value != null) {
      values.add(name);
      values.add(value);
    }
    return this;
  }
}
