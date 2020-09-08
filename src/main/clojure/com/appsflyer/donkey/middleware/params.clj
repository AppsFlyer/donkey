(ns com.appsflyer.donkey.middleware.params
  (:import (com.appsflyer.donkey.route.ring.middleware MiddlewareProvider)))

(defn keywordize-query-params [handler]
  (fn
    ([request]
     (handler (MiddlewareProvider/keywordizeQueryParams request)))
    ([request respond raise]
     (handler (MiddlewareProvider/keywordizeQueryParams request) respond raise))))
