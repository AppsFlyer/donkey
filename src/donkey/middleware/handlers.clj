(ns donkey.middleware.handlers
  (:import (io.vertx.core Handler)
           (io.vertx.ext.web RoutingContext)
           (com.appsflyer.donkey.route.handler.ring Constants)))

(deftype MiddlewareHandler [impl]
  Handler
  (handle [_this ctx]
    (letfn [(respond [res]
              (-> ^RoutingContext ctx
                  (.put Constants/LAST_HANDLER_RESPONSE_FIELD res)
                  .next))
            (raise [ex] (.fail ^RoutingContext ctx ^Throwable ex))]
      (try
        (impl (get-handler-argument ctx) respond raise)
        (catch Throwable ex
          (.fail ^RoutingContext ctx ^Throwable ex))))))

(deftype BlockingMiddlewareHandler [handler1 handler2]
  Handler
  (handle [_this ctx]
    (try
      (-> ^RoutingContext ctx
          (.put Constants/LAST_HANDLER_RESPONSE_FIELD
                (impl (get-handler-argument ^RoutingContext ctx)))
          .next)
      (catch Throwable ex
        (.fail ^RoutingContext ctx ^Throwable ex)))))
