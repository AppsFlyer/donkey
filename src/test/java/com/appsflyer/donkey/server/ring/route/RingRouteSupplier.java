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

package com.appsflyer.donkey.server.ring.route;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import com.appsflyer.donkey.server.ring.handler.RingHandler;
import com.appsflyer.donkey.server.route.PathDefinition;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.route.RouteSupplier;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Checkpoint;

import static com.appsflyer.donkey.ClojureObjectMapper.serialize;
import static com.appsflyer.donkey.server.route.PathDefinition.MatchType.REGEX;
import static io.vertx.core.http.HttpMethod.GET;

public class RingRouteSupplier implements RouteSupplier {
  
  public RingHandler returnRequest(Checkpoint requestsServed) {
    return ctx -> {
      ctx.response()
         .end(Buffer.buffer(
             serialize(ctx.get(RingHandler.RING_HANDLER_RESULT))));
      requestsServed.flag();
    };
  }
  
  private static IPersistentMap getRingHandlerResult(RoutingContext ctx) {
    return ctx.get(RingHandler.RING_HANDLER_RESULT);
  }
  
  private static IPersistentMap getPathParams(IPersistentMap request) {
    return (IPersistentMap) request.valAt(Keyword.intern("path-params"));
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
        .handler((RingHandler) ctx -> {
          ctx.response()
             .end(Buffer.buffer(
                 serialize(
                     getPathParams(
                         getRingHandlerResult(ctx)))));
          requestsServed.flag();
        });
  }
  
  @Override
  public RouteDefinition getRegexPath(Checkpoint requestsServed, String uri) {
    return RouteDefinition
        .create()
        .addMethod(GET)
        .path(PathDefinition.create(uri, REGEX))
        .handler((RingHandler) ctx -> {
          ctx.response()
             .end(Buffer.buffer(
                 serialize(
                     getPathParams(
                         getRingHandlerResult(ctx)))));
          requestsServed.flag();
        });
  }
  
}
