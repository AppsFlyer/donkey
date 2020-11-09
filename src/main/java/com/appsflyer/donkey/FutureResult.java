/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appsflyer.donkey;

import clojure.lang.*;
import io.vertx.core.Future;

import java.util.concurrent.*;

/**
 * An abstraction over {@link CompletableFuture} that makes it easier to use
 * in Clojure.
 * <p/>
 * The class allows handling a "successful" or a "failed" operation.
 * <p/>
 * A "successful" operation is considered as such if it doesn't throw an
 * exception. The {@link #onSuccess(IFn)} handler function is called with the
 * result of the previous handler as long as an exception is not thrown.
 * <p/>
 * A "failed" operation is considered as such if it throws an exception.
 * The {@link #onFail(IFn)} handler function is called when a previous handler
 * throws an exception, with the exception as its sole argument.
 * The {@code onFail} handler can decide to recover from the error by returning
 * any value. In that case any {@code onSuccess} handlers that were defined
 * <b>after</b> the {@code onFail} handler will be called with the returned
 * value.
 * If it's not possible to recover from the error, the {@code onFail} handler
 * should rethrow the exception (or a different exception).
 * <p/>
 * The {@link #onComplete(IFn)} handler can be used to handle both "successful"
 * and "failed" operations. It will be called with the result (or null) of the
 * previous handler if the operation was "successful", and the {@code Throwable}
 * (or null) if it "failed".
 *
 * @param <T>
 */
public final class FutureResult<T> implements CompletableResult<T>, IDeref, IBlockingDeref {
  
  private final CompletableFuture<Object> impl;
  
  public static <V> FutureResult<V> create() {
    return new FutureResult<>(CompletableFuture.completedFuture(null));
  }
  
  public static <V> FutureResult<V> create(V value) {
    return new FutureResult<>(CompletableFuture.completedFuture(value));
  }
  
  public static <V> FutureResult<V> create(CompletableFuture<Object> future) {
    return new FutureResult<>(future);
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
  public FutureResult<T> onComplete(IFn handler) {
    return new FutureResult<>(
        impl.handle((v, t) -> {
          Throwable ex = t;
          if (t != null) {
            ex = t.getCause() == null ? t : t.getCause();
          }
          return handler.invoke(v, ex);
        }));
  }
  
  @Override
  public FutureResult<T> onSuccess(IFn handler) {
    return new FutureResult<>(impl.thenApply(handler::invoke));
  }
  
  @Override
  public FutureResult<T> onFail(IFn handler) {
    return new FutureResult<>(
        impl.handle((v, t) -> {
          if (t == null) {
            return v;
          }
          return handler.invoke(t.getCause() == null ? t : t.getCause());
        }));
  }
  
  @Override
  public Object deref() {
    try {
      return impl.get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return new ExceptionInfo(ex.getMessage(), RT.map(), ex);
    } catch (ExecutionException ex) {
      return new ExceptionInfo(ex.getCause().getMessage(), RT.map(), ex.getCause());
    }
  }
  
  @Override
  public Object deref(long ms, Object timeoutValue) {
    try {
      return impl.get(ms, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return new ExceptionInfo(ex.getMessage(), RT.map(), ex);
    } catch (ExecutionException ex) {
      return new ExceptionInfo(ex.getCause().getMessage(), RT.map(), ex.getCause());
    } catch (TimeoutException ex) {
      return timeoutValue;
    }
  }
}
