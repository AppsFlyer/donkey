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
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterFactory {
  
  static RouterFactory create(Vertx vertx, RouteList routeList) {
    return new RouterFactoryImpl(vertx, routeList);
  }
  
  /**
   * Set the {@link RouteCreatorFactory} that will be used to create a {@link Router}
   */
  Router withRouteCreator(RouteCreatorFactory routeCreatorFactory);
}
