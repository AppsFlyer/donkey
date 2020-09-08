(ns ^:integration com.appsflyer.donkey.client-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-http.client :as clj-http]
            [com.appsflyer.donkey.client :as client]
            [com.appsflyer.donkey.test-helper :as util]
            [com.appsflyer.donkey.routes :as routes])
  (:import (io.netty.handler.codec.http HttpResponseStatus)
           (clojure.lang ExceptionInfo)
           (com.appsflyer.donkey.client.exception UnsupportedDataTypeException)))


(def route-descriptors
  [routes/root-200
   routes/echo-route
   routes/explicit-consumes-json
   routes/explicit-produces-json])

(use-fixtures :once
              util/init-donkey
              (fn [test-fn] (util/init-donkey-server test-fn route-descriptors))
              util/init-donkey-client)

(defn- make-request [opts]
  (let [-promise (promise)]
    (client/request
      util/donkey-client
      (assoc opts :handler (fn [res ex] (deliver -promise {:res res :ex ex}))))
    -promise))

(deftest test-basic-functionality
  (testing "it should get a 200 response code"
    (let [{:keys [res ex]} @(make-request {:method :get :uri "/"})]
      (is (nil? ex))
      (is (= 200 (:status res))))))

(deftest test-ring-compliant-response
  (testing "The response should include at least :status, :headers, and :body fields"
    (let [{:keys [res ex]} @(make-request {:method :get :uri "/echo"})]
      (is (nil? ex))
      (is (= 200 (:status res)))
      (is (< 0 (Integer/valueOf ^String (get-in res [:headers "content-length"]))))
      (is (bytes? (:body res))))))

(deftest test-not-found-status-code
  (testing "it should return a NOT FOUND response when a route doesn't exist"
    (let [{:keys [res ex]} @(make-request {:method :post :uri "/foo"})]
      (is (nil? ex))
      (is (= (.code HttpResponseStatus/NOT_FOUND) (:status res))))))

(deftest test-method-not-allowed-status-code
  (testing "it should return a METHOD NOT ALLOWED response when an HTTP verb is not supported"
    (let [{:keys [res ex]} @(make-request {:method :post :uri "/"})]
      (is (nil? ex))
      (is (= (.code HttpResponseStatus/METHOD_NOT_ALLOWED) (:status res))))))

(deftest test-not-acceptable-status-code
  (testing "it should return a NOT ACCEPTABLE response when a route does
  not produce an acceptable request mime type"
    (let [{:keys [res ex]} @(make-request {:method  :get
                                           :uri     "/produces/json"
                                           :headers {"accept" "text/html"}})]
      (is (nil? ex))
      (is (= (.code HttpResponseStatus/NOT_ACCEPTABLE) (:status res))))))

(deftest test-unsupported-media-type-status-code
  (testing "it should return a UNSUPPORTED MEDIA TYPE response when a route does
  not consume the requests mime type"
    (let [{:keys [res ex]} @(make-request {:method  :post
                                           :uri     "/consumes/json"
                                           :headers {"content-type" "text/plain"}
                                           :body    "Hello world!"})]
      (is (nil? ex))
      (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (:status res))))))

(deftest test-bad-request-status-code
  (testing "it should return a BAD REQUEST response when the route consumes a mime type
  and the request doesn't have a content-type"
    (let [{:keys [res ex]} @(make-request {:method :post
                                           :uri    "/consumes/json"
                                           :body   "{\"foo\":\"bar\"}"})]
      (is (nil? ex))
      (is (= (.code HttpResponseStatus/BAD_REQUEST) (:status res))))))

(deftest test-unsupported-data-type-exception
  (testing "the operation should fail when the body is not a string or byte[]"
    (let [{:keys [res ex]} @(make-request {:method :get
                                           :uri    "/"
                                           :body   {:foo "bar"}})]
      (is (nil? res))
      (is (instance? ExceptionInfo ex))
      (is (instance? UnsupportedDataTypeException (ex-cause ex))))))

(defn- parse-response-body [response]
  (let [body (:body response)]
    (if (string? body)
      (read-string body)
      (read-string (String. ^bytes body)))))

(deftest test-query-parameters
  (let [params "baz=3&foo=bar"
        expected {"baz" "3" "foo" "bar"}]
    (testing "it should parse query parameters in the uri"
      (let [{:keys [res ex]} @(make-request {:method :get, :uri (str "/echo?" params)})
            body (parse-response-body res)]
        (is (nil? ex))
        (is (= params (:query-string body)))
        (is (= expected (:query-params body)))))

    (testing "it should parse query parameters in the configuration"
      (let [{:keys [res ex]} @(make-request {:method :get, :uri "/echo", :query-params expected})
            _ (println ex)
            body (parse-response-body res)]
        (is (nil? ex))
        (is (= params (:query-string body)))
        (is (= expected (:query-params body)))))))


(deftest test-unicode-encoding
  (let [expected "高性能HTTPServer和Client"
        encoded "%E9%AB%98%E6%80%A7%E8%83%BDHTTPServer%E5%92%8CClient"
        uri (str "/echo?str=" encoded)
        opts {:method :get, :uri uri}]
    (let [{:keys [res ex]} @(make-request opts)]
      (is (nil? ex))
      (is (= expected (get-in (parse-response-body res) [:query-params "str"]))))
    (let [{:keys [res ex]} @(make-request (assoc opts :method :post
                                                      :query-params {"str" encoded}
                                                      :uri "/echo"
                                                      :headers {"content-type" "multipart/form-data"}))]
      (println (parse-response-body res))
      (is (nil? ex))
      (is (= expected (get-in (parse-response-body res) [:form-params "str"]))))
    ;(is (= u (:body (clj-http/post url {:form-params {:str u}}))))
    ;(is (= u (:body @(http/get url2))))
    ;(is (= u (:body (clj-http/get url2))))

    ))
