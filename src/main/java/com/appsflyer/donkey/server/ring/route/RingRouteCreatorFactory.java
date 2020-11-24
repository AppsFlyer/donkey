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

import com.appsflyer.donkey.server.route.AbstractRouteCreator;
import com.appsflyer.donkey.server.route.RouteCreatorFactory;
import com.appsflyer.donkey.server.route.RouteList;
import io.vertx.ext.web.Router;

public final class RingRouteCreatorFactory implements RouteCreatorFactory {
  
  public static RouteCreatorFactory create() {
    return new RingRouteCreatorFactory();
  }
  
  private RingRouteCreatorFactory() {}
  
  @Override
  public AbstractRouteCreator newInstance(Router router, RouteList routeList) {
    return RingRouteCreator.create(router, routeList);
  }
}
