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

(ns com.appsflyer.donkey.error-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.appsflyer.donkey.test-helper :as helper]
            [com.appsflyer.donkey.routes :as routes]
            [com.appsflyer.donkey.core :as donkey]
            [com.appsflyer.donkey.server :as server]
            [jsonista.core :as jsonista]
            [jsonista.core :as json])
  (:import (io.netty.handler.codec.http HttpResponseStatus)))

(use-fixtures :once helper/init-donkey helper/init-donkey-client)

(defn- start-server [error-handlers]
  (let [instance (donkey/create-server
                   helper/donkey-core
                   (merge
                     helper/default-server-options
                     {:routes         [routes/root-200
                                       routes/internal-server-error]
                      :error-handlers error-handlers}))]
    (server/start-sync instance)
    instance))

(deftest test-error-handlers
  (let [mapper (json/object-mapper {:decode-key-fn true})
        server-instance (start-server
                          {500 (fn [v]
                                 {:status 500
                                  :body   (jsonista/write-value-as-bytes v)})
                           404 (fn [v]
                                 {:status 400
                                  :body   (jsonista/write-value-as-bytes v)})
                           405 (fn [v]
                                 {:status 405
                                  :body   (jsonista/write-value-as-bytes v)})})]

    (try
      (testing "custom 404 NOT FOUND error handler returns 400 code"
        (let [res @(helper/make-request {:method :get
                                         :uri    "/not-found"})
              body (json/read-value (:body res) mapper)]
          (is (= (.code HttpResponseStatus/BAD_REQUEST) (:status res)))
          (is (= "/not-found" (:path body)))
          (is (nil? (:cause body)))))

      (testing "custom 500 INTERNAL SERVER ERROR"
        (let [res @(helper/make-request {:method :get
                                         :uri    (:path routes/internal-server-error)})
              body (json/read-value (:body res) mapper)]
          (is (= (.code HttpResponseStatus/INTERNAL_SERVER_ERROR) (:status res)))
          (is (= (:path routes/internal-server-error) (:path body)))
          (is (= "Route throws an exception" (-> body :cause :message)))))

      (testing "custom 405 METHOD NOT ALLOWED"
        (let [res @(helper/make-request {:method :post
                                         :uri    (:path routes/root-200)})
              body (json/read-value (:body res) mapper)]
          (is (= (.code HttpResponseStatus/METHOD_NOT_ALLOWED) (:status res)))
          (is (= (:path routes/root-200) (:path body)))
          (is (nil? (:cause body)))))
      (finally
        (server/stop server-instance)))))
