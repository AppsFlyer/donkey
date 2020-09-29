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

(ns com.appsflyer.donkey.metrics
  (:import (io.vertx.ext.dropwizard DropwizardMetricsOptions)
           (io.vertx.core.metrics MetricsOptions)))

(defn ^MetricsOptions get-metrics-options
  "Creates and returns a MetricsOptions object from the opts map.
  The map may contain an optional `:metrics-prefix` string that will be prepended
   to each metric. It must contain a `:metric-registry` with a Dropwizard
   `MetricRegistry` instance"
  [opts]
  (doto (DropwizardMetricsOptions.)
    (.setEnabled true)
    (.setBaseName (:metrics-prefix opts "donkey"))
    (.setMetricRegistry (:metric-registry opts))))
