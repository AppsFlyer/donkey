package com.appsflyer.donkey.route.handler.ring;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class RingHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(RingHandler.class.getName());
  private final Function<RoutingContext, ?> fun;
  
  public RingHandler(Function<RoutingContext, ?> fun)
  {
    this.fun = fun;
  }
  
  @Override
  public void handle(RoutingContext ctx)
  {
    Future<?> handlerResponse;
    try {
      handlerResponse = (Future<?>) fun.apply(ctx);
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
