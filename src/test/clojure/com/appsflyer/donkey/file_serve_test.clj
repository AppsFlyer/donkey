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

(ns com.appsflyer.donkey.file-serve-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.appsflyer.donkey.test-helper :as helper]
            [com.appsflyer.donkey.routes :as routes]
            [clojure.string])
  (:import (io.netty.handler.codec.http HttpResponseStatus)
           (io.vertx.ext.web.client HttpRequest HttpResponse)))

(def resources
  {:enable-caching  true
   :resources-root  "public"
   :max-age-seconds 60
   :index-page      "home.html"
   :routes          routes/static-resources})

(use-fixtures :once
              helper/init-donkey
              (fn [test-fn] (helper/init-donkey-server test-fn [routes/non-existing-file] resources))
              helper/init-web-client)

(deftest test-serve-non-existing-file
  (testing "it should return 500 internal server error when a file doesn't exist"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get (:path routes/non-existing-file))
          (.send (helper/create-client-handler response-promise)))
      (let [^HttpResponse res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/INTERNAL_SERVER_ERROR) (.statusCode res)))))))

(deftest test-serve-static-index-page
  (testing "it should return an home.html file"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get "/")
          (.send (helper/create-client-handler response-promise)))
      (let [^HttpResponse res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
        (is (= "text/html;charset=UTF-8" (.getHeader res "content-type")))
        (is (clojure.string/starts-with? (.bodyAsString res) "<!DOCTYPE html>"))))))

(deftest test-serve-static-json-file
  (testing "it should return a json file"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get "/hello.json")
          (.send (helper/create-client-handler response-promise)))
      (let [^HttpResponse res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
        (is (= "application/json" (.getHeader res "content-type")))
        (is (= "world" (-> res .bodyAsJsonObject (.getString "hello"))))))))

(deftest test-cached-image
  (testing ""
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get "/transparent.gif")
          (.putHeader "If-Modified-Since", "Wed, 21 Oct 2015 07:28:00 GMT")
          (.send (helper/create-client-handler response-promise)))
      (let [^HttpResponse res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
        (is (= "image/gif" (.getHeader res "content-type")))
        (is (-> res .headers (.contains "last-modified")))
        (let [cache-control (clojure.string/split (-> res .headers (.get "cache-control")) #",")]
          (is ((set (map clojure.string/trim cache-control)) "max-age=60")))))))
