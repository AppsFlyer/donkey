(ns com.appsflyer.donkey.result
  (:import (clojure.lang IFn)
           (com.appsflyer.donkey FutureResult)))

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
