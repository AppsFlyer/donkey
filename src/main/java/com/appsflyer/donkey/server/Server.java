package com.appsflyer.donkey.server;

import com.appsflyer.donkey.exception.ServerInitializationException;
import com.appsflyer.donkey.exception.ServerShutdownException;
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

public final class Server
{
  private static final Logger logger = LoggerFactory.getLogger(Server.class.getName());
  private final Vertx vertx;
  private final ServerConfig config;
  
  /**
   * Create a new instance with the given {@link ServerConfig}
   * @param config
   */
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
   * @return The Vertx instance associated with the server
   */
  public Vertx vertx()
  {
    return vertx;
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
   * @throws ServerInitializationException If the thread was interrupted while deploying the verticle,
   *                                       the operation timed out or failed in some other way.
   */
  public void startSync() throws ServerInitializationException
  {
    startSync(5, TimeUnit.SECONDS);
  }
  
  /**
   * Deploy the server verticle.
   * Blocks the calling thread until the operation succeeds or fails.
   *
   * @param timeout The duration of time to wait for the operation to complete.
   * @param unit    The duration time unit
   * @throws ServerInitializationException If the thread was interrupted while deploying the verticle,
   *                                       the operation timed out or failed in some other way.
   */
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
  
  /**
   * Stops the server by releasing all resources (threads, connections, etc') associated
   * with it.
   *
   * @return Future result that will contain the Exception in case the operation failed.
   */
  public Future<Void> shutdown()
  {
    Promise<Void> promise = Promise.promise();
    vertx.close(promise);
    return promise.future();
  }
  
  /**
   * Stops the server while blocking the calling thread.
   * Returns when all resources (threads, connections, etc') are released.
   *
   * @throws ServerShutdownException If the thread was interrupted while shutting down,
   *                                 the operation timed out or failed in some other way.
   */
  public void shutdownSync() throws ServerShutdownException
  {
    shutdownSync(5, TimeUnit.SECONDS);
  }
  
  /**
   * Stops the server while blocking the calling thread up to the given timeout duration.
   * Returns when all resources (threads, connections, etc') are released.
   *
   * @param timeout The duration of time to wait for the operation to complete.
   * @param unit    The duration time unit
   * @throws ServerShutdownException If the thread was interrupted while shutting down,
   *                                 the operation timed out or failed in some other way.
   */
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
  
  /**
   * Blocks the calling thread until the JVM gets a shutdown signal.
   * At that point shuts down the server.
   * @throws InterruptedException
   */
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
