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

(defprotocol IResult
  (on-complete [this fun])
  (on-success [this fun])
  (on-fail [this fun]))

(extend-type FutureResult
  IResult
  (on-complete [this fun]
    (.onComplete ^FutureResult this ^IFn fun))
  (on-success [this fun]
    (.onSuccess ^FutureResult this ^IFn fun))
  (on-fail [this fun]
    (.onFail ^FutureResult this ^IFn fun)))

(defn create
  ([]
   (FutureResult/create))
  ([value]
   (case (class value)
     CompletableFuture (FutureResult/create ^CompletableFuture value)
     (FutureResult/create ^Object value))))
