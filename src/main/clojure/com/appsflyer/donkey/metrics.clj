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
