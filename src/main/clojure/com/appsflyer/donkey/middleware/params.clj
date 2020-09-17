(ns com.appsflyer.donkey.middleware.params
  (:import (com.appsflyer.donkey.server.ring.middleware QueryParamsKeywordizer QueryParamsParser)))

(defn parse-query-params [handler]
  (fn
    ([request]
     (handler (-> (QueryParamsParser/getInstance) (.handle request))))
    ([request respond raise]
     (handler (-> (QueryParamsParser/getInstance) (.handle request)) respond raise))))

(defn keywordize-query-params [handler]
  (fn
    ([request]
     (handler (-> (QueryParamsKeywordizer/getInstance) (.handle request))))
    ([request respond raise]
     (handler (-> (QueryParamsKeywordizer/getInstance) (.handle request)) respond raise))))

