/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey.server.route;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Checkpoint;

import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.vertx.core.http.HttpMethod.*;

public interface RouteSupplier {
  
  default RouteDefinition helloWorld(Checkpoint requestsServed) {
    return RouteDefinition
        .create()
        .path("/")
        .addMethod(GET)
        .handler(ctx -> {
          ctx.response().end("Hello, World!");
          requestsServed.flag();
        });
  }
  
  default RouteDefinition postOrPutJson(Checkpoint requestsServed, String jsonResponse) {
    return RouteDefinition
        .create()
        .path("/json")
        .addMethod(POST)
        .addMethod(PUT)
        .addConsumes(APPLICATION_JSON.toString())
        .addProduces(APPLICATION_JSON.toString())
        .handler(ctx -> {
          ctx.response().end(jsonResponse);
          requestsServed.flag();
        });
  }
  
  default RouteDefinition postFormOrFile(Checkpoint requestsServed) {
    return RouteDefinition.create()
                          .path("/post/form")
                          .addMethod(POST)
                          .addConsumes(APPLICATION_X_WWW_FORM_URLENCODED.toString())
                          .addConsumes(MULTIPART_FORM_DATA.toString())
                          .addConsumes(APPLICATION_OCTET_STREAM.toString())
                          .handler(returnRequest(requestsServed));
  }
  
  default RouteDefinition internalServerError() {
    return RouteDefinition
        .create()
        .path("/internal-server-error")
        .handler(ctx -> {
          throw new RuntimeException("Internal server error route");
        });
  }
  
  Handler<RoutingContext> returnRequest(Checkpoint requestsServed);
  
  RouteDefinition echo(Checkpoint requestsServed);
  
  RouteDefinition getPathVariable(Checkpoint requestsServed, String uri);
  
  RouteDefinition getRegexPath(Checkpoint requestsServed, String uri);
}
