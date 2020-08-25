package com.appsflyer.donkey.route.handler.ring;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@FunctionalInterface
public interface RingHandler extends Handler<RoutingContext> {

}
