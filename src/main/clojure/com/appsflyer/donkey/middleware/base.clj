(ns com.appsflyer.donkey.middleware.base
  (:import (com.appsflyer.donkey.server.ring.middleware RingMiddleware)))

(defn make-ring-request-middleware
  "Returns a function that will call `handler` with the result of applying
  `middleware` to the `request`.
  The returned function has two arities:
  - [request]
  - [request respond raise]"
  [^RingMiddleware middleware handler ex-handler]
  (if (fn? ex-handler)
    (fn
      ([request]
       (try
         (handler (.handle middleware request))
         (catch Exception ex
           (ex-handler {:cause ex :request request}))))
      ([request respond raise]
       (try
         (handler (.handle middleware request) respond raise)
         (catch Exception ex
           (ex-handler {:cause   ex
                        :request request
                        :respond respond
                        :raise   raise})))))
    (fn
      ([request]
       (handler (.handle middleware request)))
      ([request respond raise]
       (try
         (handler (.handle middleware request) respond raise)
         (catch Exception ex
           (raise ex)))))))

(defn make-ring-response-middleware
  "Returns a function that will call `middleware` with the result of applying
  `handler` to the `request`.
  The returned function has two arities:
  - [request]
  - [request respond raise]"
  [^RingMiddleware middleware handler ex-handler]
  (if (fn? ex-handler)
    (fn
      ([request]
       (try
         (.handle middleware (handler request))
         (catch Exception ex
           (ex-handler {:cause ex :request request}))))
      ([request respond raise]
       (try
         (handler
           request
           (fn [response]
             (try
               (respond (.handle middleware response))
               (catch Exception ex
                 (ex-handler {:cause   ex
                              :request request
                              :respond respond
                              :raise   raise}))))
           raise)
         (catch Exception ex
           (ex-handler {:cause   ex
                        :request request
                        :respond respond
                        :raise   raise})))))
    (fn
      ([request]
       (.handle middleware (handler request)))
      ([request respond raise]
       (handler
         request
         (fn [response]
           (try
             (respond (.handle middleware response))
             (catch Exception ex
               (raise ex))))
         raise)))))
