(ns com.appsflyer.donkey.middleware.headers
  (:import (com.appsflyer.donkey.server.ring.middleware LowerCaseHeaderName)))

(defn lowercase-header-name [handler]
  (fn
    ([request]
     (handler (-> (LowerCaseHeaderName/getInstance) (.handle request))))
    ([request respond raise]
     (handler (-> (LowerCaseHeaderName/getInstance) (.handle request)) respond raise))))
