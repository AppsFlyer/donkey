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

(ns com.appsflyer.donkey.middleware.middleware-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.appsflyer.donkey.test-helper :as helper]
            [com.appsflyer.donkey.middleware.params :refer [keywordize-query-params
                                                            keywordize-form-params]]
            [com.appsflyer.donkey.middleware.base :refer [make-ring-request-middleware
                                                          make-ring-response-middleware]]
            [com.appsflyer.donkey.routes :as routes]
            [clojure.string])
  (:import (io.vertx.ext.web.client HttpRequest)
           (io.netty.handler.codec.http HttpResponseStatus)
           (io.vertx.core MultiMap)
           (clojure.lang ExceptionInfo)))

(defn- make-query-param-counter-middleware
  "Returns a middleware that increments the named query parameter.
  If `param-name` doesn't exist it is initialized to 1."
  [param-name]
  (let [update-fn #(update-in % [:query-params param-name] (fnil inc 0))]
    (helper/make-pre-processing-middleware
      (fn
        ([req] (update-fn req))
        ([req respond _raise] (respond (update-fn req)))))))

(defn- execute-keywordize-params-test [uri]
  (let [response-promise (promise)
        query-string "foo=bar&_count=6&:valid=true&:-empty=false&1=2&_"]
    (-> ^HttpRequest (.get helper/vertx-client (str uri "?" query-string))
        (.send (helper/create-client-handler response-promise)))

    (let [res (helper/parse-response-body-when-resolved response-promise)
          current-ns (str (the-ns *ns*))
          expected {:foo                          "bar"
                    :_count                       "6"
                    (keyword current-ns "valid")  "true"
                    (keyword current-ns "-empty") "false"
                    :1                            "2"
                    :_                            ""
                    :added-by-middleware          2}]

      (is (= query-string (:query-string res)))
      (is (= expected (:query-params res))))))

(deftest test-middleware-exception-handling
  (let [func (fn [_] (throw (ex-info "foo bar" {})))]

    (testing "it should call the supplied ex-handler function"
      (let [handler (constantly {})
            ex-handler (fn [obj]
                         (is (= "foo bar" (ex-message (:cause obj))))
                         (is (= "foo bar" (-> obj :request :foo)))
                         (is (= handler (:handler obj))))
            middleware (make-ring-request-middleware {:middleware func
                                                      :handler    handler
                                                      :ex-handler ex-handler})]
        (middleware {:foo "foo bar"})))

    (testing "it should call the supplied ex-handler function"
      (let [handler (constantly {})
            respond (fn [arg] (is (= "foobar" arg)))
            raise (fn [ex] (is (instance? ExceptionInfo ex)))
            ex-handler (fn [obj]
                         (is (= "foo bar" (ex-message (:cause obj))))
                         (is (= "foo bar" (-> obj :request :foo)))
                         (is (= handler (:handler obj)))
                         ((:respond obj) "foobar")
                         ((:raise obj) (:cause obj)))
            middleware (make-ring-request-middleware {:middleware func
                                                      :handler    handler
                                                      :ex-handler ex-handler})]
        (middleware {:foo "foo bar"} respond raise)))

    (testing "it should call the supplied ex-handler function"
      (let [handler (fn [_] (throw (ex-info "foo bar" {})))
            ex-handler (fn [obj]
                         (is (= "foo bar" (ex-message (:cause obj))))
                         (is (= "foo bar" (-> obj :request :foo)))
                         (is (= handler (:handler obj))))
            middleware (make-ring-response-middleware
                         {:middleware func
                          :handler    handler
                          :ex-handler ex-handler})]
        (middleware {:foo "foo bar"})))

    (testing "it should call the supplied ex-handler function"
      (let [handler (fn [& _] (throw (ex-info "foo bar" {})))
            respond (fn [arg] (is (= "foobar" arg)))
            raise (fn [ex] (is (instance? ExceptionInfo ex)))
            ex-handler (fn [obj]
                         (is (= "foo bar" (ex-message (:cause obj))))
                         (is (= "foo bar" (-> obj :request :foo)))
                         (is (= handler (:handler obj)))
                         ((:respond obj) "foobar")
                         ((:raise obj) (:cause obj)))
            middleware (make-ring-response-middleware
                         {:middleware func
                          :handler    handler
                          :ex-handler ex-handler})]
        (middleware {:foo "foo bar"} respond raise)))))

(deftest test-keywordize-query-params
  (testing "it should turn string query parameters keys into keywords and call the
  next middleware."
    (helper/run-with-server-and-client
      (fn []
        (execute-keywordize-params-test (:path routes/echo-route))
        (execute-keywordize-params-test (:path routes/echo-route-non-blocking)))
      [routes/echo-route routes/echo-route-non-blocking]
      [(keywordize-query-params)
       (make-query-param-counter-middleware :added-by-middleware)
       (make-query-param-counter-middleware :added-by-middleware)])))

(defn- execute-keywordize-form-params-test [uri]
  (let [response-promise (promise)
        form-params (doto (MultiMap/caseInsensitiveMultiMap)
                      (.add "brand" "Pim")
                      (.add "status" "On")
                      (.add "message" "Yoohoo")
                      (.add "type" "user")
                      (.add "product" "vv9xoB90"))]

    (-> ^HttpRequest (.post helper/vertx-client uri)
        (.sendForm form-params (helper/create-client-handler response-promise)))

    (let [res (helper/parse-response-body-when-resolved response-promise)]
      (is (= {:brand   "Pim"
              :status  "On"
              :message "Yoohoo"
              :type    "user"
              :product "vv9xoB90"}
             (:form-params res))))))

(deftest test-keywordize-form-params
  (testing "it should turn string form parameters keys into keywords and call the
  next middleware."
    (helper/run-with-server-and-client
      (fn []
        (execute-keywordize-form-params-test (:path routes/echo-route))
        (execute-keywordize-form-params-test (:path routes/echo-route-non-blocking)))
      [routes/echo-route routes/echo-route-non-blocking]
      [(keywordize-form-params)])))

(defn- execute-multiple-middleware-per-route-test [uri]
  (let [response-promise (promise)]
    (-> helper/vertx-client
        ^HttpRequest (.get uri)
        (.send (helper/create-client-handler response-promise)))

    (let [res (helper/wait-for-response response-promise)]
      (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
      (let [res-json (.bodyAsJsonObject res)]
        (is (= true (.getBoolean res-json "success")))
        (is (= 3 (.getInteger res-json "counter")))))))

(deftest multiple-middleware-per-route-test
  (testing "it should call each middleware with the result of the previous"
    (helper/run-with-server-and-client
      (fn []
        (execute-multiple-middleware-per-route-test
          (:path routes/blocking-middleware-handlers))
        (execute-multiple-middleware-per-route-test
          (:path routes/non-blocking-middleware-handlers)))
      [routes/blocking-middleware-handlers
       routes/non-blocking-middleware-handlers])))

(defn- execute-multiple-middleware-per-route-exception-test [uri]
  (let [response-promise (promise)]
    (-> helper/vertx-client
        ^HttpRequest (.get uri)
        (.send (helper/create-client-handler response-promise)))

    (let [res (helper/wait-for-response response-promise)]
      (is (= (.code HttpResponseStatus/INTERNAL_SERVER_ERROR) (.statusCode res))))))

(deftest multiple-middleware-per-route-exception-test
  (testing "it should return an internal server error when an exception is thrown by
  one of the middleware"
    (helper/run-with-server-and-client
      (fn []
        (execute-multiple-middleware-per-route-exception-test
          (:path routes/blocking-exceptional-middleware-handlers))
        (execute-multiple-middleware-per-route-exception-test
          (:path routes/non-blocking-exceptional-middleware-handlers)))
      [routes/blocking-exceptional-middleware-handlers
       routes/non-blocking-exceptional-middleware-handlers])))
