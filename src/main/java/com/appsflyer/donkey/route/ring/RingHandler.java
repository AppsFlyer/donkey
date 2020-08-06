package com.appsflyer.donkey.route.ring;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import com.appsflyer.donkey.route.handler.ring.RingResponseHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RingHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(RingHandler.class.getName());
  private final IFn fun;
  
  public RingHandler(IFn fun)
  {
    this.fun = fun;
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    Future<?> handlerResponse;
    try {
      handlerResponse = (Future<?>) fun.invoke(ctx);
    } catch (Exception ex) {
      logger.error(String.format("User handler failed: %s", ex.getMessage()), ex);
      ctx.fail(ex);
      return;
    }
    handlerResponse
        .onSuccess(res -> {
          ctx.put("ring-response", res);
          ctx.next();
        })
        .onFailure(ctx::fail);
  }
}
