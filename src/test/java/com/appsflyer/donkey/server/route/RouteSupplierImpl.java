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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Checkpoint;

import static com.appsflyer.donkey.server.route.PathDefinition.MatchType.REGEX;
import static io.vertx.core.http.HttpMethod.GET;

public class RouteSupplierImpl implements RouteSupplier {
  
  private static final ObjectMapper mapper = new ObjectMapper();
  
  @Override
  public Handler<RoutingContext> returnRequest(Checkpoint requestsServed) {
    return ctx -> {
      try {
        ctx.response()
           .end(Buffer.buffer(
               mapper.writeValueAsBytes(ctx.request())));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      requestsServed.flag();
    };
  }
  
  @Override
  public RouteDefinition echo(Checkpoint requestsServed) {
    return RouteDefinition.create()
                           .path("/echo")
                           .handler(returnRequest(requestsServed));
  }
  
  @Override
  public RouteDefinition getPathVariable(Checkpoint requestsServed, String uri) {
    return RouteDefinition
        .create()
        .addMethod(GET)
        .path(uri)
        .handler(ctx -> {
          try {
            ctx.response()
               .end(Buffer.buffer(
                   mapper.writeValueAsBytes(ctx.pathParams())));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          requestsServed.flag();
        });
  }
  
  @Override
  public RouteDefinition getRegexPath(Checkpoint requestsServed, String uri) {
    return RouteDefinition
        .create()
        .addMethod(GET)
        .path(PathDefinition.create(uri, REGEX))
        .handler(ctx -> {
          try {
            ctx.response()
               .end(Buffer.buffer(
                   mapper.writeValueAsBytes(ctx.pathParams())));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          requestsServed.flag();
        });
  }
}
