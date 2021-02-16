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

(ns com.appsflyer.donkey.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.appsflyer.donkey.core :refer [create-donkey
                                               create-server
                                               create-client
                                               destroy]]
            [com.appsflyer.donkey.server :refer [start-sync stop-sync]]
            [com.appsflyer.donkey.client :refer [request]]
            [com.appsflyer.donkey.request :refer [submit]]
            [com.appsflyer.donkey.test-helper :as helper])
  (:import (com.appsflyer.donkey.core Donkey)
           (java.net ConnectException)))

(deftest test-create-donkey
  (testing "it should create a Donkey instance"
    (is (instance? Donkey (create-donkey)))))

(deftest test-destroy-donkey
  (testing "it should release all resources associated with the Donkey instance"
    (let [make-client #(create-client % {:default-port helper/DEFAULT-PORT})
          call-server #(-> % (request {:method :get}) submit)
          donkey (create-donkey)
          server (create-server donkey {:port   helper/DEFAULT-PORT
                                        :routes [{:handler (fn [_, res _] (res {:status 200 :body "hello world"}))}]})
          client (make-client donkey)]

      (start-sync server)
      (is (= 200 (-> client call-server deref :status)))

      @(destroy donkey)

      ;; Check that the client was closed
      (is (thrown? IllegalStateException @(call-server client)))

      ;; Create a new client and verify that the server is unreachable
      (is (= ConnectException (->
                                (create-donkey)
                                make-client
                                call-server
                                deref
                                ex-cause
                                ex-cause
                                type))))))
