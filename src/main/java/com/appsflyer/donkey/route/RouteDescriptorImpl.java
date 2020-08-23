package com.appsflyer.donkey.route;

import com.appsflyer.donkey.route.handler.Middleware;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

public class RouteDescriptorImpl implements RouteDescriptor {
  
  private final Collection<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
  private final Collection<String> consumes = new HashSet<>(6);
  private final Collection<String> produces = new HashSet<>(6);
  private HandlerMode handlerMode = HandlerMode.NON_BLOCKING;
  private Handler<RoutingContext> handler;
  private Middleware middleware;
  private PathDescriptor path;
  
  @Override
  public PathDescriptor path() {
    return path;
  }
  
  @Override
  public RouteDescriptor path(PathDescriptor path) {
    this.path = path;
    return this;
  }
  
  @Override
  public Collection<HttpMethod> methods() {
    if (methods.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(methods);
  }
  
  @Override
  public RouteDescriptor addMethod(HttpMethod method) {
    Objects.requireNonNull(method, "method cannot be null");
    methods.add(method);
    return this;
  }
  
  @Override
  public Collection<String> consumes() {
    if (consumes.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(consumes);
  }
  
  @Override
  public RouteDescriptor addConsumes(String contentType) {
    assertNonEmptyContentType(contentType);
    consumes.add(contentType);
    return this;
  }
  
  @Override
  public Collection<String> produces() {
    if (produces.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(produces);
  }
  
  @Override
  public RouteDescriptor addProduces(String contentType) {
    assertNonEmptyContentType(contentType);
    produces.add(contentType);
    return this;
  }
  
  @Override
  public Handler<RoutingContext> handler() {
    if (handler == null) {
      throw new IllegalStateException("No handlers were set");
    }
    return handler;
  }
  
  @Override
  public RouteDescriptor addHandler(Handler<RoutingContext> handler) {
    Objects.requireNonNull(handler, "Handler cannot be null");
    this.handler = handler;
    return this;
  }
  
  @Override
  public HandlerMode handlerMode() {
    return handlerMode;
  }
  
  @Override
  public Middleware middleware() {
    return middleware;
  }
  
  @Override
  public RouteDescriptor middleware(Middleware middleware) {
    this.middleware = middleware;
    return this;
  }
  
  @Override
  public boolean hasMiddleware() {
    return middleware != null;
  }
  
  @Override
  public RouteDescriptor handlerMode(HandlerMode handlerMode) {
    Objects.requireNonNull(handlerMode, "handler mode cannot be null");
    this.handlerMode = handlerMode;
    return this;
  }
  
  private void assertNonEmptyContentType(String val) {
    Objects.requireNonNull(val, "contentType cannot be null");
    if (val.isBlank()) {
      throw new IllegalArgumentException(String.format("Invalid content type: %s", val));
    }
  }
}
