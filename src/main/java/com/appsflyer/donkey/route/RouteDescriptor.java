package com.appsflyer.donkey.route;

import clojure.lang.IFn;
import io.vertx.core.http.HttpMethod;

import java.util.*;

public class RouteDescriptor
{
  private PathDescriptor path;
  private final Collection<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
  private final Collection<String> consumes = new HashSet<>(6);
  private final Collection<String> produces = new HashSet<>(6);
  private HandlerMode handlerMode = HandlerMode.NON_BLOCKING;
  private IFn handler;
  
  public PathDescriptor path()
  {
    return path;
  }
  
  public RouteDescriptor path(PathDescriptor path)
  {
    Objects.requireNonNull(path, "path cannot be null");
    this.path = path;
    return this;
  }
  
  public List<HttpMethod> methods()
  {
    if (methods.isEmpty()) {
      return Collections.emptyList();
    }
    return List.copyOf(methods);
  }
  
  public RouteDescriptor addMethod(HttpMethod method)
  {
    Objects.requireNonNull(method, "method cannot be null");
    methods.add(method);
    return this;
  }
  
  public List<String> consumes()
  {
    if (consumes.isEmpty()) {
      return Collections.emptyList();
    }
    return List.copyOf(consumes);
  }
  
  public RouteDescriptor addConsumes(String contentType)
  {
    assertNonEmptyContentType(contentType);
    consumes.add(contentType);
    return this;
  }
  
  public List<String> produces()
  {
    if (produces.isEmpty()) {
      return Collections.emptyList();
    }
    return List.copyOf(produces);
  }
  
  public RouteDescriptor addProduces(String contentType)
  {
    assertNonEmptyContentType(contentType);
    produces.add(contentType);
    return this;
  }
  
  public IFn handler()
  {
    return handler;
  }
  
  public RouteDescriptor handler(IFn handler)
  {
    Objects.requireNonNull(handler, "handler cannot be null");
    this.handler = handler;
    return this;
  }
  
  public HandlerMode handlerMode()
  {
    return handlerMode;
  }
  
  public RouteDescriptor handlerMode(HandlerMode handlerMode)
  {
    Objects.requireNonNull(handlerMode, "handler mode cannot be null");
    this.handlerMode = handlerMode;
    return this;
  }
  
  private void assertNonEmptyContentType(String val)
  {
    Objects.requireNonNull(val, "contentType cannot be null");
    if (val.isBlank()) {
      throw new IllegalArgumentException(String.format("Invalid content type: %s", val));
    }
  }
}
