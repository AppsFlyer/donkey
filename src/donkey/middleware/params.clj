(ns donkey.middleware.params
  (:import (com.appsflyer.donkey.middleware MiddlewareProvider)))

(defn keywordize-query-params
  ([request]
   (MiddlewareProvider/keywordizeQueryParams request))
  ([request respond raise]
   (try
     (respond (MiddlewareProvider/keywordizeQueryParams request))
     (catch Exception ex
       (raise ex)))))
