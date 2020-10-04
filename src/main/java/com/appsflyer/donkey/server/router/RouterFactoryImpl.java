/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.router;

import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.handler.NotFoundErrorHandler;
import com.appsflyer.donkey.server.handler.InternalServerErrorHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import java.util.Objects;

public class RouterFactoryImpl implements RouterFactory {
  
  private final Router router;
  private final RouteList routeList;
  
  RouterFactoryImpl(Vertx vertx, RouteList routeList) {
    Objects.requireNonNull(vertx, "Vertx argument is missing");
    Objects.requireNonNull(routeList, "Route list argument is missing");
    
    this.routeList = routeList;
    router = Router.router(vertx);
    router.errorHandler(500, new InternalServerErrorHandler())
          .errorHandler(404, new NotFoundErrorHandler());
  }
  
  @Override
  public Router withRouteCreator(RouteCreatorFactory routeCreatorFactory) {
    return routeCreatorFactory.newInstance(router, routeList).addRoutes();
  }
}
