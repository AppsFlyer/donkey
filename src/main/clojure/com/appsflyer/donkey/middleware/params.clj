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
  (:import (com.appsflyer.donkey.server.ring.middleware QueryParamsKeywordizer QueryParamsParser)))

(defn parse-query-params [handler]
  (fn
    ([request]
     (handler (.handle (QueryParamsParser/getInstance) request)))
    ([request respond raise]
     (handler (.handle (QueryParamsParser/getInstance) request) respond raise))))

(defn keywordize-query-params [handler]
  (fn
    ([request]
     (handler (.handle (QueryParamsKeywordizer/getInstance) request)))
    ([request respond raise]
     (try
       (handler
         (.handle (QueryParamsKeywordizer/getInstance) request)
         respond
         raise)
       (catch Exception ex
         (raise ex))))))
