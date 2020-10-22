;
; Copyright 2020 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns com.appsflyer.donkey.middleware.params
  (:import (com.appsflyer.donkey.server.ring.middleware QueryParamsKeywordizer
                                                        QueryParamsParser
                                                        FormParamsKeywordizer
                                                        FormParamsKeywordizer$Options
                                                        RingMiddleware)))

(defn- make-ring-middleware [^RingMiddleware middleware handler]
  (fn
    ([request]
     (handler (.handle middleware request)))
    ([request respond raise]
     (try
       (handler (.handle middleware request) respond raise)
       (catch Exception ex
         (raise ex))))))

(defn parse-query-params [handler]
  (make-ring-middleware (QueryParamsParser/getInstance) handler))

(defn keywordize-query-params [handler]
  (make-ring-middleware (QueryParamsKeywordizer/getInstance) handler))

(defn keywordize-form-params
  ([] (keywordize-form-params {:deep true}))
  ([opts]
   (fn [handler]
     (make-ring-middleware
       (FormParamsKeywordizer. (FormParamsKeywordizer$Options. (:deep opts)))
       handler))))


