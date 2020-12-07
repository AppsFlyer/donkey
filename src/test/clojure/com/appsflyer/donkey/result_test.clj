;
; Copyright 2020 AppsFlyer
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
;

(ns ^:integration com.appsflyer.donkey.result-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.appsflyer.donkey.result :as result])
  (:import (java.util.concurrent CompletableFuture)
           (java.util.function Supplier)
           (com.appsflyer.donkey FutureResult)))

(deftest test-create
  (is (instance? FutureResult (result/create)))
  (is (instance? FutureResult (result/create "foo"))))

(deftest test-on-complete
  (testing "it should successfully complete asynchronously"
    (let [p (promise)
          expected-success "success"
          success-handler (fn [[value ex]]
                            (is (= expected-success value))
                            (is (nil? ex)))]
      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] expected-success))))
          (result/on-complete (fn [val ex] (deliver p [val ex]))))
      (success-handler @p)))

  (testing "it should complete with an exception asynchronously"
    (let [p (promise)
          expected-fail (RuntimeException. "failed")
          fail-handler (fn [[value ex]]
                         (is (= (.getMessage ^Throwable expected-fail)
                                (.getMessage ^Throwable ex)))
                         (is (nil? value)))]
      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] (throw expected-fail)))))
          (result/on-complete (fn [val ex] (deliver p [val ex]))))
      (fail-handler @p))))

(deftest test-on-success
  (let [p (promise)
        expected-success "success"
        success-handler (fn [value] (is (= expected-success value)))]

    (testing "it should call the success handler only when the operation
    completes successfully"
      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] expected-success))))
          (result/on-success (fn [val] (deliver p val))))
      (success-handler @p))))

(deftest test-on-fail
  (let [p (promise)
        expected-fail (RuntimeException. "failed")
        fail-handler (fn [ex] (is (= (.getMessage ^Throwable expected-fail)
                                     (.getMessage ^Throwable ex))))]

    (-> (result/create
          (CompletableFuture/supplyAsync
            (reify Supplier
              (get [_this] (throw expected-fail)))))
        (result/on-fail (fn [ex] (deliver p ex))))
    (fail-handler @p)))

(deftest complete-handler-chaining
  (testing "it should call the next handler with the current handler's return value"
    (let [p (promise)
          expected-value 3
          complete-handler (fn [[value ex]]
                             (is (== expected-value value))
                             (is (nil? ex)))]

      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] 1))))
          (result/on-complete (fn [val _ex] (inc val)))
          (result/on-complete (fn [val _ex] (inc val)))
          (result/on-complete (fn [val ex] (deliver p [val ex]))))
      (complete-handler @p))))

(deftest success-handler-chaining
  (testing "it should call the next `on-success` handler with the current
  handler's return value"
    (let [p (promise)
          expected-value 3
          success-handler (fn [value] (is (== expected-value value)))]

      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] 1))))
          (result/on-success inc)
          (result/on-success inc)
          (result/on-success (fn [val] (deliver p val))))
      (success-handler @p))))

(deftest fail-handler-chaining
  (testing "it should call the next `on-fail` handler as long as an Exception is
  thrown"
    (let [p (promise)
          expected-fail-message "fail fail fail"
          fail-handler (fn [ex] (is (= expected-fail-message (.getMessage ^Throwable ex))))]

      (-> (result/create
            (CompletableFuture/supplyAsync
              (reify Supplier
                (get [_this] (throw (RuntimeException. "fail"))))))
          (result/on-success (fn [_] (is false) "on-success should not be called"))
          (result/on-fail (fn [ex] (throw (RuntimeException. (str (.getMessage ^Throwable ex) " fail")))))
          (result/on-fail (fn [ex] (throw (RuntimeException. (str (.getMessage ^Throwable ex) " fail")))))
          (result/on-fail (fn [ex] (deliver p ex))))
      (fail-handler @p))))
