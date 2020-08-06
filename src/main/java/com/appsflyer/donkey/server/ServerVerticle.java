package com.appsflyer.donkey.server;

import com.appsflyer.donkey.route.RouterFactory;
import io.vertx.core.*;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerVerticle extends AbstractVerticle
{
  private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class.getName());
  private final ServerConfig config;
  
  ServerVerticle(ServerConfig config)
  {
    this.config = config;
  }
  
  @Override
  public void start(Promise<Void> promise)
  {
    vertx.createHttpServer(config.serverOptions())
         .requestHandler(createRouter())
         .listen(res -> {
           if (res.failed()) {
             logger.error(res.cause().getMessage(), res.cause());
             promise.fail(res.cause());
           }
           else {
             promise.complete();
           }
         });
  }
  
  private Router createRouter()
  {
    return new RouterFactory(vertx, config.handlerFactory())
        .withRoutes(config.routes());
  }
}
