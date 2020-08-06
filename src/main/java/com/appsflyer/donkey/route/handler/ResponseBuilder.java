package com.appsflyer.donkey.route.handler;

import com.appsflyer.donkey.util.DateHeaderGenerator;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;

public class ResponseBuilder
{
  private final DateHeaderGenerator dateHeaderGenerator;
  private static final String SERVER_NAME = "Donkey";
  
  public ResponseBuilder(Vertx vertx)
  {
    dateHeaderGenerator = new DateHeaderGenerator(vertx).start();
  }
  
  protected void addDefaultHeaders(HttpServerResponse response)
  {
      addDateHeader(response);
      addServerHeader(response);
  }
  void addDateHeader(HttpServerResponse response)
  {
    response.headers().add(HttpHeaders.DATE, dateHeaderGenerator.get());
  }
  
  void addServerHeader(HttpServerResponse response)
  {
    response.headers().add(HttpHeaders.SERVER, SERVER_NAME);
  }
}
