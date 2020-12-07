;
; Copyright 2020 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
;

(ns com.appsflyer.donkey.result
  (:import (clojure.lang IFn)
           (com.appsflyer.donkey FutureResult)
           (java.util.concurrent CompletableFuture)))

(defn on-complete
  "The `on-complete` handler can be used to handle both 'successful'
   and 'failed' operations. It will be called with the result (or nil) of the
   previous handler if the operation was 'successful', and the `Throwable`
   (or nil) if it 'failed'."
  [^FutureResult res func]
  (.onComplete res ^IFn func))

(defn on-success
  "Called when the `FutureResult` succeeds, or previous handler does not 'fail'.
   A 'successful' operation is considered as such if it doesn't throw an
   exception. The `on-success` handler function is called with the
   result of the previous handler as long as an exception is not thrown."
  [^FutureResult res func]
  (.onSuccess res ^IFn func))

(defn on-fail
  "Called when the `FutureResult` fails, or a previous handler throws an
   exception. The handler is called with the exception as its sole argument.
   The `on-fail` handler can decide to recover from the error by returning any
   value. In that case any `on-success` handlers that were defined
   after the `on-fail` handler will be called with the returned value.
   If it's not possible to recover from the error, the `on-fail` handler
   should rethrow the exception (or a different exception)."
  [^FutureResult res func]
  (.onFail res ^IFn func))

(defn create
  ([]
   (FutureResult/create))
  ([value]
   (if (identical? (class value) CompletableFuture)
     (FutureResult/create ^CompletableFuture value)
     (FutureResult/create ^Object value))))
