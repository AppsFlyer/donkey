;
; Copyright 2020-2021 AppsFlyer
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

(ns ^:integration com.appsflyer.donkey.client-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.appsflyer.donkey.middleware.params :as params]
            [com.appsflyer.donkey.test-helper :as helper]
            [com.appsflyer.donkey.routes :as routes]
            [com.appsflyer.donkey.core :as donkey]
            [com.appsflyer.donkey.client :as client])
  (:import (io.netty.handler.codec.http HttpResponseStatus)
           (clojure.lang ExceptionInfo)
           (com.appsflyer.donkey.client.exception UnsupportedDataTypeException)
           (java.nio.charset StandardCharsets)))

(def route-maps
  [routes/root-200
   routes/echo-route
   routes/explicit-consumes-json
   routes/explicit-produces-json
   routes/redirects-to-root])

(use-fixtures :once
              helper/init-donkey
              (fn [test-fn] (helper/init-donkey-server test-fn route-maps nil [(params/parse-query-params)]))
              helper/init-donkey-client)

(deftest test-basic-functionality
  (testing "it should get a 200 response code"
    (let [res @(helper/make-request {:method :get :uri "/"})]
      (is (= 200 (:status res))))))

(deftest test-ring-compliant-response
  (testing "The response should include at least :status, :headers, and :body fields"
    (let [res @(helper/make-request {:method :get :uri "/echo"})]
      (is (= 200 (:status res)))
      (is (< 0 (Integer/valueOf ^String (get-in res [:headers "content-length"]))))
      (is (bytes? (:body res))))))

(deftest test-not-found-status-code
  (testing "it should return a NOT FOUND response when a route doesn't exist"
    (let [res @(helper/make-request {:method :post :uri "/foo"})]
      (is (= (.code HttpResponseStatus/NOT_FOUND) (:status res))))))

(deftest test-method-not-allowed-status-code
  (testing "it should return a METHOD NOT ALLOWED response when an HTTP verb is not supported"
    (let [res @(helper/make-request {:method :post :uri "/"})]
      (is (= (.code HttpResponseStatus/METHOD_NOT_ALLOWED) (:status res))))))

(deftest test-not-acceptable-status-code
  (testing "it should return a NOT ACCEPTABLE response when a route does
  not produce an acceptable request mime type"
    (let [res @(helper/make-request {:method  :get
                                     :uri     "/produces/json"
                                     :headers {"accept" "text/html"}})]
      (is (= (.code HttpResponseStatus/NOT_ACCEPTABLE) (:status res))))))

(deftest test-unsupported-media-type-status-code
  (testing "it should return an UNSUPPORTED MEDIA TYPE response when a route does
  not consume the requests mime type"
    (let [res @(helper/make-request {:method  :post
                                     :uri     "/consumes/json"
                                     :headers {"content-type" "text/plain"}}
                                    "Hello world!")]
      (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (:status res))))))

(deftest test-bad-request-status-code
  (testing "it should return a BAD REQUEST response when the route consumes a mime type
  and the request doesn't have a content-type"
    (let [res @(helper/make-request {:method :post
                                     :uri    "/consumes/json"}
                                    "{\"foo\":\"bar\"}")]
      (is (= (.code HttpResponseStatus/BAD_REQUEST) (:status res))))))

(deftest test-unsupported-data-type-exception
  (testing "the operation should fail when the body is not a string or byte[]"
    (let [ex @(helper/make-request {:method :get
                                    :uri    "/"}
                                   {:foo "bar"})]
      (is (instance? ExceptionInfo ex))
      (is (instance? UnsupportedDataTypeException (ex-cause ex))))))

(defn- parse-response-body [response]
  (let [body (:body response)]
    (if (string? body)
      (read-string body)
      (read-string (String. ^bytes body StandardCharsets/UTF_8)))))

(deftest test-query-parameters
  (testing "it should parse query parameters in the uri"
    (let [params "baz=3&foo=bar"
          expected {"baz" "3" "foo" "bar"}
          res @(helper/make-request {:method :get, :uri (str "/echo?" params)})
          body (parse-response-body res)]
      (is (= params (:query-string body)))
      (is (= expected (:query-params body)))))

  (testing "it should parse query parameters in the configuration and add them
    to the url"
    (let [res @(helper/make-request {:method       :get,
                                     :uri          "/echo?key=value",
                                     :query-params {"baz" "3" "foo" "bar"}})
          body (parse-response-body res)
          expected-query-string "key=value&baz=3&foo=bar"
          expected-query-params {"baz" "3" "foo" "bar" "key" "value"}]
      (is (= expected-query-string (:query-string body)))
      (is (= expected-query-params (:query-params body))))))

(deftest test-unicode-decoding
  (let [expected "高性能HTTPServer和Client"
        encoded "%E9%AB%98%E6%80%A7%E8%83%BDHTTPServer%E5%92%8CClient"]

    (testing "it should decode urlencoded strings in the url and body"
      (let [res @(helper/make-request {:method :get, :uri (str "/echo?str=" encoded)})]
        (is (= expected (get-in (parse-response-body res) [:query-params "str"]))))

      (let [res @(helper/submit-form {:method :post :uri "/echo"} {"str" encoded})]
        (is (= expected (get-in (parse-response-body res) [:form-params "str"])))))))

(deftest test-urlencoded-forms
  (testing "the form fields should be encoded by the client and then decoded
  by the server so they are identical both places."
    (let [fields {"name"  "John Smith"
                  "email" "john@smithcorp.com"
                  "text"  "Hey! I am John -> {how} about & this?"}]
      (let [res @(helper/submit-form {:method :post :uri "/echo"} fields)]
        (let [body (parse-response-body res)]
          (is (= "application/x-www-form-urlencoded" (get-in body [:headers "content-type"])))
          (is (= fields (:form-params body))))))))

(deftest test-absolute-url
  (testing "it should return a 200 response"
    (let [res @(helper/make-request {:method :get,
                                     :url    (str "http://localhost:" helper/DEFAULT-PORT "/")})]
      (is (= 200 (:status res))))
    (let [res @(helper/make-request {:method :get,
                                     :url    (str "http://localhost:" helper/DEFAULT-PORT)})]
      (is (= 200 (:status res))))))

(deftest test-follow-redirect
  (testing "it should follow the redirect response from the server and return a 200 response"
    (let [res @(helper/make-request {:method :get, :uri (:path routes/redirects-to-root)})]
      (is (= 200 (:status res))))))

(deftest test-does-not-follow-redirect
  (testing "it should return a 302 response"
    (let [opts (assoc helper/default-client-options :follow-redirects false)]
      (binding [helper/donkey-client (donkey/create-client helper/donkey-core opts)]
        (let [res @(helper/make-request {:method :get, :uri (:path routes/redirects-to-root)})]
          (is (= 302 (:status res)))
          (client/stop helper/donkey-client))))))

