package com.appsflyer.donkey.server;

import com.appsflyer.donkey.exception.ServerInitializationException;
import com.appsflyer.donkey.exception.ServerShutdownException;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class Server
{
  private static final Logger logger = LoggerFactory.getLogger(Server.class.getName());
  private final Vertx vertx;
  private final ServerConfig config;
  
  public Server(ServerConfig config)
  {
    vertx = vertx(config);
    this.config = config;
  }
  
  private Vertx vertx(ServerConfig config)
  {
    return Vertx.vertx(config.vertxOptions())
                .exceptionHandler(ex -> logger.error(ex.getMessage(), ex.getCause()));
  }
  
  /**
   * Deploy the server verticle.
   *
   * @return Future of the verticle deployment id
   */
  public Future<String> start()
  {
    Promise<String> promise = Promise.promise();
    var deploymentOptions =
        new DeploymentOptions()
            .setInstances(config.vertxOptions().getEventLoopPoolSize());
    vertx.deployVerticle(() -> new ServerVerticle(config), deploymentOptions, promise);
    
    return promise.future();
  }
  
  /**
   * Deploy the server verticle.
   * Blocks the calling thread until the operation succeeds or fails.
   *
   * @throws ServerInitializationException when server initialization fails.
   */
  public void startSync() throws ServerInitializationException
  {
    var latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    
    start().onComplete(v -> {
      if (v.failed()) {
        error.set(v.cause());
      }
      latch.countDown();
    });
    
    try {
      latch.await();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerInitializationException(
          "Thread interrupted during initialization", ex);
    }
    
    if (error.get() != null) {
      throw new ServerInitializationException(error.get());
    }
  }
  
  public Future<Void> shutdown()
  {
    Promise<Void> promise = Promise.promise();
    vertx.close(promise);
    return promise.future();
  }
  
  public void shutdownSync() throws ServerShutdownException
  {
    var latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    
    shutdown().onComplete(v -> {
      if (v.failed()) {
        error.set(v.cause());
      }
      latch.countDown();
    });
    
    try {
      latch.await();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerShutdownException("Thread interrupted during shutdown", ex);
    }
    
    if (error.get() != null) {
      throw new ServerShutdownException(error.get());
    }
  }
  
  public void awaitTermination() throws InterruptedException
  {
    var latch = new CountDownLatch(1);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        shutdownSync();
      } catch (ServerShutdownException ex) {
        logger.warn(ex.getMessage());
      }
      latch.countDown();
    }));
    
    latch.await();
  }
}
