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
  (:gen-class)
  (:require [criterium.core :as cc]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [com.appsflyer.donkey.middleware.params :refer [parse-query-params
                                                            keywordize-query-params]]))

(declare -main)

(def ^:private my-ns (-> (meta #'-main) :ns str))

(defmacro title [benchmark]
  (let [benchmark# (symbol my-ns (str benchmark))]
    `(println (str "Running benchmark for '" (:name (meta (var ~benchmark#))) "' ..."))))

;Evaluation count : 11308800 in 60 samples of 188480 calls.
;Execution time sample mean : 5.349326 µs
;Execution time mean : 5.349414 µs
;Execution time sample std-deviation : 46.974663 ns
;Execution time std-deviation : 48.000457 ns
;Execution time lower quantile : 5.299997 µs ( 2.5%)
;Execution time upper quantile : 5.462938 µs (97.5%)
;Overhead used : 6.156983 ns
(defn bench-ring-warp-keyword-params []
  (title bench-ring-warp-keyword-params)

  (let [middleware ((comp wrap-params wrap-keyword-params) identity)]
    (cc/bench
      (middleware
        {:query-string "foo=bar&city=New%20York&occupation=Shop%20Keeper&age=49"})
      :verbose)))

; =============================================================

;Evaluation count : 87036480 in 60 samples of 1450608 calls.
;Execution time sample mean : 695.467996 ns
;Execution time mean : 695.434175 ns
;Execution time sample std-deviation : 8.472041 ns
;Execution time std-deviation : 8.547805 ns
;Execution time lower quantile : 686.252122 ns ( 2.5%)
;Execution time upper quantile : 715.160931 ns (97.5%)
;Overhead used : 5.771017 ns
(defn bench-donkey-keywordize-query-params []
  (title bench-donkey-keywordize-query-params)

  (let [middleware ((comp (parse-query-params) (keywordize-query-params)) identity)]
    (cc/bench
      (middleware
        {:query-string "foo=bar&city=New%20York&occupation=Shop%20Keeper&age=49"})
      :verbose)))

; =============================================================

(defn- run-all []
  (bench-ring-warp-keyword-params)
  (bench-donkey-keywordize-query-params))

(defn -main [& args]
  (let [ran (atom false)]
    (doseq [fn-name args]
      (when-let [func (resolve (symbol my-ns (str fn-name)))]
        (reset! ran true)
        (func)))

    (when (not @ran)
      (println "Running all benchmarks")
      (run-all))))
