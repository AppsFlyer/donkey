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

(ns com.appsflyer.donkey.benchmark
  (:require [criterium.core :as cc]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [com.appsflyer.donkey.middleware.params :refer [parse-query-params
                                                            keywordize-query-params]]))


;Evaluation count : 10900800 in 60 samples of 181680 calls.
;Execution time lower quantile : 5.418234 µs ( 2.5%)
;Execution time upper quantile : 5.632535 µs (97.5%)
(defn bench-ring-warp-keyword-params []
  (let [middleware ((comp wrap-params wrap-keyword-params) identity)]
    (cc/with-progress-reporting
      (cc/bench
        (middleware
          {:query-string "foo=bar&city=New%20York&occupation=Shop%20Keeper&age=49"})
        :verbose))))

; =============================================================

;Evaluation count : 69935820 in 60 samples of 1165597 calls.
;Execution time lower quantile : 806.790313 ns ( 2.5%)
;Execution time upper quantile : 840.258526 ns (97.5%)
(defn bench-donkey-keywordize-query-params []
  (let [middleware ((comp parse-query-params keywordize-query-params) identity)]
    (cc/with-progress-reporting
      (cc/bench
        (middleware
          {:query-string "foo=bar&city=New%20York&occupation=Shop%20Keeper&age=49"})
        :verbose))))
