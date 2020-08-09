package com.appsflyer.donkey.route;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.function.Function;

public interface RouteDescriptor
{
  PathDescriptor path();
  
  RouteDescriptor path(PathDescriptor path);
  
  Collection<HttpMethod> methods();
  
  RouteDescriptor addMethod(HttpMethod method);
  
  Collection<String> consumes();
  
  RouteDescriptor addConsumes(String contentType);
  
  Collection<String> produces();
  
  RouteDescriptor addProduces(String contentType);
  
  Function<RoutingContext, ?> handler();
  
  RouteDescriptor handler(Function<RoutingContext, ?> handler);
  
  HandlerMode handlerMode();
  
  RouteDescriptor handlerMode(HandlerMode handlerMode);
}
