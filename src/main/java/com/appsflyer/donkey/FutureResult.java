package com.appsflyer.donkey;

import clojure.lang.ExceptionInfo;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.RT;
import io.vertx.core.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class FutureResult<T> implements CompletableResult<T>, IDeref {
  
  private final CompletableFuture<Object> impl;
  
  public static <V> FutureResult<V> create() {
    return new FutureResult<>(new CompletableFuture<>());
  }
  
  public static <V> FutureResult<V> create(V value) {
    return new FutureResult<>(CompletableFuture.completedFuture(value));
  }
  
  public static <V> FutureResult<V> create(Future<V> vertxFuture) {
    var impl = new CompletableFuture<>();
    vertxFuture.onComplete(event -> {
      if (event.succeeded()) {
        impl.complete(event.result());
      } else {
        impl.completeExceptionally(event.cause());
      }
    });
    
    return new FutureResult<>(impl);
  }
  
  private FutureResult(CompletableFuture<Object> impl) {
    this.impl = impl;
  }
  
  @Override
  public CompletableResult<T> onComplete(IFn handler) {
    return new FutureResult<>(impl.whenComplete(handler::invoke));
  }
  
  @Override
  public CompletableResult<T> onSuccess(IFn handler) {
    return new FutureResult<>(impl.thenApply(handler::invoke));
  }
  
  @Override
  public CompletableResult<T> onFail(IFn handler) {
    return new FutureResult<>(impl.exceptionally(handler::invoke));
  }
  
  @Override
  public Object deref() {
    try {
      return impl.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return new ExceptionInfo(e.getMessage(), RT.map(), e);
    } catch (ExecutionException e) {
      return new ExceptionInfo(e.getCause().getMessage(), RT.map(), e.getCause());
    }
  }
}
