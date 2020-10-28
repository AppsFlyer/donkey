package com.appsflyer.donkey;

import clojure.lang.IFn;

public interface CompletableResult<T> {
  
  CompletableResult<T> onComplete(IFn handler);
  
  CompletableResult<T> onSuccess(IFn handler);
  
  CompletableResult<T> onFail(IFn handler);
}
