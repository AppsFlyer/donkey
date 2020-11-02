(ns com.appsflyer.donkey.middleware.base
  (:import (com.appsflyer.donkey.server.ring.middleware RingMiddleware)))

(defn- normalize-middleware [exec]
  (if (instance? RingMiddleware exec)
    exec
    (if (fn? exec)
      (reify RingMiddleware
        (handle [_this val]
          (exec val)))
      (throw
        (ex-info
          "middleware must be a function or implement RingMiddleware" {:arg exec})))))

(defn make-ring-request-middleware
  "Returns a function that will call `handler` with the result of applying
  `middleware` to the `request`.
  The returned function has two arities:
  - [request]
  - [request respond raise]
  Accepts a map with the following keys:
  - middleware [fn|RingMiddleware] A function or an instance of RingMiddleware
    that will be called with the request map.
  - handler [fn] The next handler of the request
  - ex-handler [fn] Optional. A function that will be called if an exception is
    thrown. The function will get a single map argument with the following keys:
    - :cause [Throwable] The caught exception.
    - :request [map] The request map
    - :handler [fn] The next handler function. Can be used to recover from the
      exception.
    - respond [fn] Only for the three arity middleware.
    - raise [fn] Only for the three arity middleware.
    "
  [{:keys [middleware handler ex-handler]}]
  (let [^RingMiddleware middleware# (normalize-middleware middleware)]
    (if (fn? ex-handler)
      (fn
        ([request]
         (try
           (handler (.handle middleware# request))
           (catch Exception ex
             (ex-handler {:cause   ex
                          :request request
                          :handler handler}))))
        ([request respond raise]
         (try
           (handler (.handle middleware# request) respond raise)
           (catch Exception ex
             (ex-handler {:cause   ex
                          :handler handler
                          :request request
                          :respond respond
                          :raise   raise})))))
      (fn
        ([request]
         (handler (.handle middleware# request)))
        ([request respond raise]
         (try
           (handler (.handle middleware# request) respond raise)
           (catch Exception ex
             (raise ex))))))))

(defn make-ring-response-middleware
  "Returns a function that will call `middleware` with the result of applying
  `handler` to the `request`.
  The returned function has two arities:
  - [request]
  - [request respond raise]
  Accepts a map with the following keys:
  - middleware [fn|RingMiddleware] A function or an instance of RingMiddleware
  that will be called with the request map.
  - handler [fn] The next handler of the request
  - ex-handler [fn] Optional. A function that will be called if an exception is
  thrown. The function will get a single map argument with the following keys:
  - :cause [Throwable] The caught exception.
  - :request [map] The request map
  - :handler [fn] The next handler function. Can be used to recover from the
  exception.
  - respond [fn] Only for the three arity middleware.
  - raise [fn] Only for the three arity middleware."
  [{:keys [middleware handler ex-handler]}]
  (let [^RingMiddleware middleware# (normalize-middleware middleware)]
    (if (fn? ex-handler)
      (fn
        ([request]
         (try
           (.handle middleware# (handler request))
           (catch Exception ex
             (ex-handler {:cause   ex
                          :request request
                          :handler handler}))))
        ([request respond raise]
         (try
           (handler
             request
             (fn [response]
               (try
                 (respond (.handle middleware# response))
                 (catch Exception ex
                   (ex-handler {:cause   ex
                                :handler handler
                                :request request
                                :respond respond
                                :raise   raise}))))
             raise)
           (catch Exception ex
             (ex-handler {:cause   ex
                          :handler handler
                          :request request
                          :respond respond
                          :raise   raise})))))
      (fn
        ([request]
         (.handle middleware# (handler request)))
        ([request respond raise]
         (handler
           request
           (fn [response]
             (try
               (respond (.handle middleware# response))
               (catch Exception ex
                 (raise ex))))
           raise))))))
