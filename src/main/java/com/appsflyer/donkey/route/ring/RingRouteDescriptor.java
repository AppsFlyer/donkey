package com.appsflyer.donkey.route.ring;

import com.appsflyer.donkey.route.HandlerMode;
import com.appsflyer.donkey.route.PathDescriptor;
import com.appsflyer.donkey.route.RouteDescriptor;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

public class RingRouteDescriptor implements RouteDescriptor
{
  private final Collection<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
  private final Collection<String> consumes = new HashSet<>(6);
  private final Collection<String> produces = new HashSet<>(6);
  private final Collection<Handler<RoutingContext>> handlers = new ArrayList<>(10);
  private PathDescriptor path;
  private HandlerMode handlerMode = HandlerMode.NON_BLOCKING;
  
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
  public Collection<Handler<RoutingContext>> handlers()
  {
    if (handlers.isEmpty()) {
      throw new IllegalStateException("No handlers were set");
    }
    return List.copyOf(handlers);
  }
  
  @Override
  public RingRouteDescriptor addHandler(Handler<RoutingContext> handler)
  {
    Objects.requireNonNull(handler, "Handler cannot be null");
    handlers.add(handler);
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
