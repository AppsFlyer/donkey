package com.appsflyer.donkey.server;

import com.appsflyer.donkey.server.exception.ServerInitializationException;
import com.appsflyer.donkey.server.exception.ServerShutdownException;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ServerImpl implements Server {
  private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class.getName());
  private final Vertx vertx;
  private final ServerConfig config;
  private static final String THREAD_CHECKS_PROP_NAME = "vertx.threadChecks";
  private static final String DISABLE_TIMINGS_PROP_NAME = "vertx.disableContextTimings";
  private static final String DISABLE_TCCL_PROP_NAME = "vertx.disableTCCL";
  
  ServerImpl(ServerConfig config)
  {
    vertx = vertx(config);
    this.config = config;
  }
  
  private Vertx vertx(ServerConfig config)
  {
    return Vertx.vertx(config.vertxOptions())
                .exceptionHandler(ex -> logger.error(ex.getMessage(), ex.getCause()));
  }
 
  @Override
  public Vertx vertx()
  {
    return vertx;
  }
  
  @Override
  public Future<String> start()
  {
    Promise<String> promise = Promise.promise();
    var deploymentOptions =
        new DeploymentOptions()
            .setInstances(config.vertxOptions().getEventLoopPoolSize());
    vertx.deployVerticle(() -> new ServerVerticle(config), deploymentOptions, promise);
    
    return promise.future();
  }
  
  @Override
  public void startSync() throws ServerInitializationException
  {
    startSync(5, TimeUnit.SECONDS);
  }
  
  @Override
  public void startSync(int timeout, TimeUnit unit) throws
                                                    ServerInitializationException
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
      if (!latch.await(timeout, unit)) {
        throw new ServerInitializationException(
            String.format("Server start up timed out after %d %s",
                          timeout, unit.name().toLowerCase(Locale.ENGLISH)));
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerInitializationException(
          "Thread interrupted during initialization", ex);
    }
    
    if (error.get() != null) {
      throw new ServerInitializationException(error.get());
    }
  }
  
  @Override
  public Future<Void> shutdown()
  {
    Promise<Void> promise = Promise.promise();
    vertx.close(promise);
    return promise.future();
  }
  
  @Override
  public void shutdownSync() throws ServerShutdownException
  {
    shutdownSync(5, TimeUnit.SECONDS);
  }
  
  @Override
  public void shutdownSync(int timeout, TimeUnit unit) throws
                                                       ServerShutdownException
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
      if (!latch.await(timeout, unit)) {
        throw new ServerShutdownException(
            String.format("Server shutdown timed out after %d %s",
                          timeout, unit.name().toLowerCase(Locale.ENGLISH)));
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ServerShutdownException("Thread interrupted during shutdown", ex);
    }
    
    if (error.get() != null) {
      throw new ServerShutdownException(error.get());
    }
  }
  
  @Override
  public void awaitTermination() throws InterruptedException
  {
    var latch = new CountDownLatch(1);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        shutdownSync();
      } catch (ServerShutdownException ex) {
        //noinspection UseOfSystemOutOrSystemErr The JVM is shutting down and the logger may not be available
        System.err.println(ex.getMessage());
      }
      latch.countDown();
    }));
    
    latch.await();
  }
}
