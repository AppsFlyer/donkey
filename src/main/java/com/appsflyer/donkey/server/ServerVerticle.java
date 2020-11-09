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

package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.router.RouterFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerVerticle extends AbstractVerticle {
  
  private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class.getName());
  private final ServerConfig config;
  
  ServerVerticle(ServerConfig config) {
    this.config = config;
  }
  
  @Override
  public void start(Promise<Void> promise) {
    vertx.createHttpServer(config.serverOptions())
         .requestHandler(createRouter())
         .listen(res -> {
           if (res.failed()) {
             logger.error(res.cause().getMessage(), res.cause());
             promise.fail(res.cause());
           } else {
             promise.complete();
           }
         });
  }
  
  private Router createRouter() {
    return RouterFactory.create(vertx, config.routeList())
                        .withRouteCreator(config.routeCreatorFactory());
  }
}
