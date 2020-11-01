package com.appsflyer.donkey;

import clojure.lang.ExceptionInfo;
import clojure.lang.IFn;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FutureResultTest {
  
  private static final Answer<Integer> incrementImpl =
      invocation -> (int) invocation.getArgument(0) + 1;
  
  private static final Answer<Integer> incrementBy10Impl =
      invocation -> (int) invocation.getArgument(0) + 10;
  
  private static final Answer<Integer> exceptionThrower =
      invocation -> { throw new RuntimeException(); };
  
  private static final Answer<Integer> exceptionReThrower =
      invocation -> {
        for (var t : invocation.getArguments()) {
          if (t instanceof Throwable) {
            throw (Throwable) t;
          }
        }
        return null;
      };
  
  @Test
  void testOnComplete_noArgConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create().onComplete(fn);
    verify(fn).invoke(isNull(), isNull());
  }
  
  @Test
  void testOnComplete_VConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(true).onComplete(fn);
    verify(fn).invoke(anyBoolean(), isNull());
  }
  
  @Test
  void testOnComplete_SucceededFutureConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(Future.succeededFuture(1)).onComplete(fn);
    verify(fn).invoke(anyInt(), isNull());
  }
  
  @Test
  void testOnComplete_FailedFutureConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(Future.failedFuture("oops")).onComplete(fn);
    verify(fn).invoke(isNull(), isA(Throwable.class));
  }
  
  @Test
  void testOnSuccess_noArgConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create().onSuccess(fn);
    verify(fn).invoke(isNull());
  }
  
  @Test
  void testOnSuccess_VConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create("foo").onSuccess(fn);
    verify(fn).invoke(anyString());
  }
  
  @Test
  void testOnSuccess_SucceededFutureConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(Future.succeededFuture(new ArrayList<>())).onSuccess(fn);
    verify(fn).invoke(anyList());
  }
  
  @Test
  void testOnSuccess_FailedFutureConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(Future.failedFuture(new RuntimeException())).onSuccess(fn);
    verify(fn, never()).invoke(any());
  }
  
  @Test
  void testOnFail_noArgConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create().onFail(fn);
    verify(fn, never()).invoke(any());
  }
  
  @Test
  void testOnFail_VConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create("foo").onFail(fn);
    verify(fn, never()).invoke(any());
  }
  
  @Test
  void testOnFail_SucceededFutureConstructor() {
    IFn fn = mock(IFn.class);
    FutureResult.create(Future.succeededFuture(new ArrayList<>())).onFail(fn);
    verify(fn, never()).invoke(any());
  }
  
  @Test
  void testOnFail_FailedFutureConstructor() {
    IFn fn = mock(IFn.class);
    Throwable ex = new RuntimeException();
    FutureResult.create(Future.failedFuture(ex)).onFail(fn);
    verify(fn).invoke(eq(ex));
  }
  
  @Test
  void testChainedOnComplete() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt(), any())).thenAnswer(incrementImpl);
    
    FutureResult.create(0)
                .onComplete(increment)
                .onComplete(increment)
                .onComplete(increment);
    
    verify(increment).invoke(eq(0), isNull());
    verify(increment).invoke(eq(1), isNull());
    verify(increment).invoke(eq(2), isNull());
  }
  
  @Test
  void testChainedOnSuccess() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    FutureResult.create(0)
                .onSuccess(increment)
                .onSuccess(increment)
                .onSuccess(increment);
    
    verify(increment).invoke(eq(0));
    verify(increment).invoke(eq(1));
    verify(increment).invoke(eq(2));
  }
  
  @Test
  void testChainedOnFail() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    FutureResult.create(0)
                .onFail(increment)
                .onFail(increment)
                .onFail(increment);
    
    verify(increment, never()).invoke(anyInt());
  }
  
  @Test
  void testChainedOnSuccessAndOnComplete() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    when(increment.invoke(anyInt(), any())).thenAnswer(incrementImpl);
    
    IFn incrementBy10 = mock(IFn.class);
    when(incrementBy10.invoke(anyInt())).thenAnswer(incrementBy10Impl);
    when(incrementBy10.invoke(anyInt(), any())).thenAnswer(incrementBy10Impl);
    
    FutureResult.create(0)
                .onSuccess(increment)       // 0
                .onComplete(incrementBy10)  // 1, null
                .onSuccess(increment)       // 11
                .onSuccess(incrementBy10)   // 12
                .onComplete(increment);     // 22, null
    
    verify(increment).invoke(eq(0));
    verify(incrementBy10).invoke(eq(1), isNull());
    verify(increment).invoke(eq(11));
    verify(incrementBy10).invoke(eq(12));
    verify(increment).invoke(eq(22), isNull());
  }
  
  @Test
  void testChainedOnFailAndOnComplete() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt(), isNull())).thenAnswer(incrementImpl);
    when(increment.invoke(isNull(), isA(Throwable.class))).thenAnswer(exceptionReThrower);
    
    IFn throwsException = mock(IFn.class);
    when(throwsException.invoke(anyInt(), isNull())).thenAnswer(exceptionThrower);
    
    IFn failHandler = mock(IFn.class);
    when(failHandler.invoke(isA(Throwable.class))).thenAnswer(exceptionReThrower);
    
    FutureResult.create(0)
                .onComplete(increment)        // 0, null
                .onFail(failHandler)          // Should not be called
                .onComplete(increment)        // 1, null
                .onComplete(throwsException)  // 2, null
                .onComplete(increment)        // null, Throwable
                .onFail(failHandler)          // Throwable
                .onComplete(increment);       // null, Throwable
    
    verify(increment).invoke(eq(0), isNull());
    verify(failHandler, never()).invoke(anyInt());
    verify(increment).invoke(eq(1), isNull());
    verify(throwsException).invoke(eq(2), isNull());
    verify(increment, times(2)).invoke(isNull(), isA(Throwable.class));
    verify(failHandler).invoke(isA(Throwable.class));
  }
  
  @Test
  void testChainedOnSuccessAndOnFail() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    IFn throwsException = mock(IFn.class);
    when(throwsException.invoke(anyInt())).thenAnswer(exceptionThrower);
    
    IFn failHandler = mock(IFn.class);
    when(failHandler.invoke(isA(Throwable.class))).thenAnswer(exceptionReThrower);
    
    FutureResult.create(0)
                .onSuccess(increment)        // 0
                .onFail(failHandler)         // Should not be called
                .onSuccess(increment)        // 1
                .onSuccess(throwsException)  // 2
                .onSuccess(increment)        // Should not be called
                .onFail(failHandler)         // Throwable
                .onSuccess(increment);       // Should not be called
    
    verify(increment).invoke(eq(0));
    verify(failHandler, never()).invoke(anyInt());
    verify(increment).invoke(eq(1));
    verify(throwsException).invoke(eq(2));
    verify(increment, never()).invoke(eq(2));
    verify(failHandler).invoke(isA(Throwable.class));
  }
  
  @Test
  void testChainedOnSuccessOnFailAndOnComplete() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    when(increment.invoke(anyInt(), isNull())).thenAnswer(incrementImpl);
    when(increment.invoke(isNull(), isA(Throwable.class))).thenAnswer(exceptionReThrower);
    
    IFn throwsException = mock(IFn.class);
    when(throwsException.invoke(anyInt())).thenAnswer(exceptionThrower);
    
    IFn failHandler = mock(IFn.class);
    when(failHandler.invoke(isA(Throwable.class))).thenAnswer(exceptionReThrower);
    
    FutureResult.create(0)
                .onFail(failHandler)        // Should not be called
                .onComplete(increment)      // 0, null
                .onSuccess(increment)       // 1
                .onComplete(increment)      // 2, null
                .onSuccess(throwsException) // 3
                .onComplete(increment)      // null, Throwable
                .onSuccess(increment)       // Should not be called
                .onFail(failHandler);       // Throwable
    
    verify(failHandler, never()).invoke(anyInt());
    verify(increment).invoke(eq(0), isNull());
    verify(increment).invoke(eq(1));
    verify(increment).invoke(eq(2), isNull());
    verify(throwsException).invoke(eq(3));
    verify(increment, never()).invoke(eq(3));
    verify(increment).invoke(isNull(), isA(Throwable.class));
    verify(failHandler).invoke(isA(Throwable.class));
  }
  
  @Test
  void testOnFailRecovery() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    IFn throwsException = mock(IFn.class);
    when(throwsException.invoke(anyInt())).thenAnswer(exceptionThrower);
    
    IFn failHandler = mock(IFn.class);
    when(failHandler.invoke(isA(Throwable.class))).thenReturn(0);
    
    FutureResult.create(0)
                .onSuccess(increment)       // 0
                .onSuccess(increment)       // 1
                .onSuccess(throwsException) // 2
                .onSuccess(increment)       // Should not be called
                .onSuccess(increment)       // Should not be called
                .onFail(failHandler)        // Throwable
                .onSuccess(increment)       // 0
                .onSuccess(increment);      // 1
    
    
    verify(increment, times(2)).invoke(eq(0));
    verify(increment, times(2)).invoke(eq(1));
    verify(throwsException).invoke(eq(2));
    verify(increment, never()).invoke(eq(2));
    verify(failHandler).invoke(isA(Throwable.class));
  }
  
  @Test
  void testOnCompleteRecovery() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt(), isNull())).thenAnswer(incrementImpl);
    
    IFn throwsException = mock(IFn.class);
    when(throwsException.invoke(anyInt(), isNull())).thenAnswer(exceptionThrower);
    
    IFn failHandler = mock(IFn.class);
    when(failHandler.invoke(isA(Throwable.class))).thenReturn(0);
    when(failHandler.invoke(isNull(), isA(Throwable.class))).thenReturn(0);
    
    FutureResult.create(0)
                .onComplete(increment)        // 0, null
                .onComplete(increment)        // 1, null
                .onComplete(throwsException)  // 2, null
                .onComplete(failHandler)      // null, Throwable
                .onFail(failHandler)          // Should not be called
                .onComplete(increment)        // 0, null
                .onComplete(increment);       // 1, null
    
    
    verify(increment, times(2)).invoke(eq(0), isNull());
    verify(increment, times(2)).invoke(eq(1), isNull());
    verify(throwsException).invoke(eq(2), isNull());
    verify(increment, never()).invoke(eq(2), isNull());
    verify(failHandler).invoke(isNull(), isA(Throwable.class));
    verify(failHandler, never()).invoke(isA(Throwable.class));
  }
  
  @Test
  void testDerefReturnsResult() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    int result = (int) FutureResult.create(0)
                                   .onSuccess(increment)
                                   .onSuccess(increment)
                                   .deref();
    assertEquals(2, result);
  }
  
  @Test
  void testDerefThrowsInterruptedException() throws Exception {
    var throwsInterruptedException = mock(CompletableFuture.class);
    when(throwsInterruptedException.get()).thenThrow(new InterruptedException());
    
    @SuppressWarnings("unchecked")
    Throwable result = (Throwable)
        FutureResult.create(throwsInterruptedException).deref();
    
    assertThat(result, instanceOf(ExceptionInfo.class));
    assertThat(result.getCause(), instanceOf(InterruptedException.class));
  }
  
  @Test
  void testDerefThrowsExecutionException() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    IFn throwsExecutionException = mock(IFn.class);
    when(throwsExecutionException.invoke(anyInt())).thenThrow(new RuntimeException());
    
    Throwable result = (Throwable) FutureResult.create(0)
                                               .onSuccess(increment)
                                               .onSuccess(throwsExecutionException)
                                               .deref();
    
    assertThat(result, instanceOf(ExceptionInfo.class));
    assertThat(result.getCause(), instanceOf(RuntimeException.class));
  }
  
  @Test
  void testDerefWithTimeoutReturnsResult() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    int result = (int) FutureResult.create(0)
                                   .onSuccess(increment)
                                   .onSuccess(increment)
                                   .deref(100, null);
    assertEquals(2, result);
  }
  
  @Test
  void testDerefReturnsDefaultValueWhenTimeoutExceeds() throws Exception {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    var task = mock(CompletableFuture.class);
    when(task.get(anyLong(), any())).thenThrow(new TimeoutException());
    
    var timeoutReturnValue = new Object();
    @SuppressWarnings("unchecked")
    Object result = FutureResult.create(task).deref(100, timeoutReturnValue);
    assertEquals(timeoutReturnValue, result);
  }
  
  @Test
  void testDerefWithTimeoutThrowsInterruptedException() throws Exception {
    var throwsInterruptedException = mock(CompletableFuture.class);
    when(throwsInterruptedException.get(anyLong(), any()))
        .thenThrow(new InterruptedException());
    
    @SuppressWarnings("unchecked")
    Throwable result = (Throwable)
        FutureResult.create(throwsInterruptedException).deref(100, null);
    
    assertThat(result, instanceOf(ExceptionInfo.class));
    assertThat(result.getCause(), instanceOf(InterruptedException.class));
  }
  
  @Test
  void testDerefWithTimeoutThrowsExecutionException() {
    IFn increment = mock(IFn.class);
    when(increment.invoke(anyInt())).thenAnswer(incrementImpl);
    
    IFn throwsExecutionException = mock(IFn.class);
    when(throwsExecutionException.invoke(anyInt()))
        .thenThrow(new RuntimeException());
    
    Throwable result = (Throwable) FutureResult.create(0)
                                               .onSuccess(increment)
                                               .onSuccess(throwsExecutionException)
                                               .deref(100, null);
    
    assertThat(result, instanceOf(ExceptionInfo.class));
    assertThat(result.getCause(), instanceOf(RuntimeException.class));
  }
}
