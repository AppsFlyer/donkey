package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;

public interface RouteDescriptor {
  
  static RouteDescriptor create() {
    return new RouteDescriptorImpl();
  }
  
  PathDescriptor path();
  
  RouteDescriptor path(PathDescriptor path);
  
  Collection<HttpMethod> methods();
  
  RouteDescriptor addMethod(HttpMethod method);
  
  Collection<String> consumes();
  
  RouteDescriptor addConsumes(String contentType);
  
  Collection<String> produces();
  
  RouteDescriptor addProduces(String contentType);
  
  Handler<RoutingContext> handler();
  
  RouteDescriptor handler(Handler<RoutingContext> handler);
  
  HandlerMode handlerMode();
  
  RouteDescriptor handlerMode(HandlerMode handlerMode);
}
