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

package com.appsflyer.donkey.server.ring.route;

import com.appsflyer.donkey.server.route.AbstractRouteCreator;
import com.appsflyer.donkey.server.route.RouteDefinition;
import com.appsflyer.donkey.server.handler.AdapterFactory;
import com.appsflyer.donkey.server.route.RouteList;
import com.appsflyer.donkey.server.ring.handler.RingAdapterFactory;
import com.appsflyer.donkey.server.ring.handler.RingHandler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class RingRouteCreator extends AbstractRouteCreator {
  
  private final AdapterFactory adapterFactory;
  
  RingRouteCreator(Router router, RouteList routeList) {
    super(router, routeList);
    adapterFactory = new RingAdapterFactory();
  }
  
  @Override
  protected void buildRoute(Route route, RouteDefinition rd) {
    setPath(route, rd);
    if (rd.handler() instanceof RingHandler) {
      addBodyHandler(route);
      addRequestAdapter(route);
      addHandler(route, rd.handler(), rd.handlerMode());
      addResponseAdapter(route);
    } else {
      addHandler(route, rd.handler(), rd.handlerMode());
    }
  }
  
  private void addRequestAdapter(Route route) {
    route.handler(adapterFactory.requestAdapter());
  }
  
  private void addResponseAdapter(Route route) {
    route.handler(adapterFactory.responseAdapter());
  }
}
