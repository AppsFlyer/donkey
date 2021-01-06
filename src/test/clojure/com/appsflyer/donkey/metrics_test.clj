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

(ns com.appsflyer.donkey.metrics-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.appsflyer.donkey.metrics :as metrics])
  (:import (com.codahale.metrics MetricRegistry)
           (io.vertx.core.metrics MetricsOptions)))

(deftest test-dropwizard-registry
  (testing "it should convert a map to a DropwizardMetricsOptions instance"
    (let [opts (metrics/map->MetricsOptions
                 {:metrics-prefix  "foo"
                  :metric-registry (MetricRegistry.)})]
      (is (instance? MetricsOptions opts))
      (is (.isEnabled ^MetricsOptions opts)))))
