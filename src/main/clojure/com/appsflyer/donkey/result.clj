(ns com.appsflyer.donkey.result
  (:import (io.vertx.core Future AsyncResult Handler)
           (clojure.lang IDeref)))

(deftype CompleteHandler [fun]
  Handler
  (handle [_this event]
    (if (.succeeded ^AsyncResult event)
      (fun (.result ^AsyncResult event) nil)
      (fun nil (ex-info (-> ^AsyncResult event .cause .getMessage) {} (.cause ^AsyncResult event))))))

(deftype SuccessHandler [fun]
  Handler
  (handle [_this response]
    (fun response)))

(deftype FailureHandler [fun]
  Handler
  (handle [_this ex]
    (fun ^Throwable ex)))

(defprotocol IResult
  (on-complete [this fun])
  (on-success [this fun])
  (on-fail [this fun]))

(deftype FutureResult [^Future impl]
  IResult
  (on-complete [this fun]
    (.onComplete ^Future impl (->CompleteHandler fun))
    this)
  (on-success [this fun]
    (.onSuccess ^Future impl (->SuccessHandler fun))
    this)
  (on-fail [this fun]
    (.onFailure ^Future impl (->FailureHandler fun))
    this)

  IDeref
  (deref [_this]
    (let [p (promise)]
      (.onComplete ^Future impl (->CompleteHandler (fn [res ex] (deliver p (or res ex)))))
      @p)))
