package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.HandlerMode;
import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.function.Function;

public class RingRouteDescriptor implements RouteDescriptor
{
  private PathDescriptor path;
  private final Collection<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
  private final Collection<String> consumes = new HashSet<>(6);
  private final Collection<String> produces = new HashSet<>(6);
  private HandlerMode handlerMode = HandlerMode.NON_BLOCKING;
  private Function<RoutingContext, ?> handler;
  
  @Override
  public PathDescriptor path()
  {
    return path;
  }
  
  @Override
  public RingRouteDescriptor path(PathDescriptor path)
  {
    this.path = path;
    return this;
  }
  
  @Override
  public Collection<HttpMethod> methods()
  {
    if (methods.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(methods);
  }
  
  @Override
  public RingRouteDescriptor addMethod(HttpMethod method)
  {
    Objects.requireNonNull(method, "method cannot be null");
    methods.add(method);
    return this;
  }
  
  @Override
  public Collection<String> consumes()
  {
    if (consumes.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(consumes);
  }
  
  @Override
  public RingRouteDescriptor addConsumes(String contentType)
  {
    assertNonEmptyContentType(contentType);
    consumes.add(contentType);
    return this;
  }
  
  @Override
  public Collection<String> produces()
  {
    if (produces.isEmpty()) {
      return Collections.emptySet();
    }
    return Set.copyOf(produces);
  }
  
  @Override
  public RingRouteDescriptor addProduces(String contentType)
  {
    assertNonEmptyContentType(contentType);
    produces.add(contentType);
    return this;
  }
  
  @Override
  public Function<RoutingContext, ?> handler()
  {
    if (handler == null) {
      throw new IllegalStateException("Route handler is not set");
    }
    return handler;
  }
  
  @Override
  public RingRouteDescriptor handler(Function<RoutingContext, ?> handler)
  {
    Objects.requireNonNull(handler, "handler cannot be null");
    this.handler = handler;
    return this;
  }
  
  @Override
  public HandlerMode handlerMode()
  {
    return handlerMode;
  }
  
  @Override
  public RingRouteDescriptor handlerMode(HandlerMode handlerMode)
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
