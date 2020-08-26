package com.appsflyer.donkey;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestUtil {
  
  public static final int DEFAULT_PORT = 16969;
  
  private TestUtil() {}
  
  public static SocketAddress getDefaultAddress() {
    return SocketAddress.inetSocketAddress(DEFAULT_PORT, "localhost");
  }
  
  public static void assert200(HttpResponse<Buffer> response) {
    assertEquals(OK.code(), response.statusCode());
  }
  
  public static void assert404(HttpResponse<Buffer> response) {
    assertEquals(NOT_FOUND.code(), response.statusCode(),
                 "It should respond with Not Found");
  }
  
  public static void assert405(HttpResponse<Buffer> response) {
    assertEquals(METHOD_NOT_ALLOWED.code(), response.statusCode(),
                 "It should respond with Method Not Allowed");
  }
  
  public static void assert406(HttpResponse<Buffer> response) {
    assertEquals(NOT_ACCEPTABLE.code(), response.statusCode(),
                 "It should respond with Not Acceptable");
  }
  
  public static void assert415(HttpResponse<Buffer> response) {
    assertEquals(UNSUPPORTED_MEDIA_TYPE.code(), response.statusCode(),
                 "It should respond with Unsupported Media Type");
  }
  
  public static Future<HttpResponse<Buffer>> doGet(Vertx vertx, String uri) {
    return makeRequest(WebClient.create(vertx), GET, uri);
  }
  
  public static Future<HttpResponse<Buffer>> doGet(WebClient client, String uri) {
    return makeRequest(client, GET, uri);
  }
  
  public static Future<HttpResponse<Buffer>> doPost(WebClient client, String uri) {
    return makeRequest(client, POST, uri);
  }
  
  private static Future<HttpResponse<Buffer>> makeRequest(
      WebClient client, HttpMethod method, String uri) {
    
    Promise<HttpResponse<Buffer>> promise = Promise.promise();
    client.request(method, getDefaultAddress(), uri)
          .send(asyncResultHandler(promise));
    
    return promise.future();
  }
  
  private static Handler<AsyncResult<HttpResponse<Buffer>>> asyncResultHandler(
      Promise<HttpResponse<Buffer>> promise) {
    
    return asyncResult -> {
      if (asyncResult.failed()) {
        promise.fail(asyncResult.cause());
      } else {
        promise.complete(asyncResult.result());
      }
    };
  }
}
