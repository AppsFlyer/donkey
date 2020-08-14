package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.util.DateHeaderGenerator;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public abstract class ResponseHandler implements Handler<RoutingContext>
{
  private final DateHeaderGenerator dateHeaderGenerator;
  private static final String SERVER_NAME = "Donkey";
  
  protected ResponseHandler(Vertx vertx)
  {
    dateHeaderGenerator = new DateHeaderGenerator(vertx).start();
  }
  
  protected void addDefaultHeaders(HttpServerResponse response)
  {
    addDateHeader(response);
    addServerHeader(response);
  }
  
  private void addDateHeader(HttpServerResponse response)
  {
    response.headers().add(HttpHeaders.DATE, dateHeaderGenerator.get());
  }
  
  private void addServerHeader(HttpServerResponse response)
  {
    response.headers().add(HttpHeaders.SERVER, SERVER_NAME);
  }
}
