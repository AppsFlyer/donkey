package com.appsflyer.donkey.route.ring;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingRingHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(BlockingRingHandler.class.getName());
  private final IFn fun;
  
  public BlockingRingHandler(IFn fun)
  {
    this.fun = fun;
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    IPersistentMap response;
    try {
      response = (IPersistentMap) fun.invoke(ctx);
    } catch (Exception ex) {
      logger.error(String.format("User handler failed: %s", ex.getMessage()), ex);
      ctx.fail(ex);
      return;
    }
    ctx.put("ring-response", response);
    ctx.next();
  }
}
