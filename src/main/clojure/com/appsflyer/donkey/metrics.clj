(ns com.appsflyer.donkey.metrics
  (:import (io.vertx.ext.dropwizard DropwizardMetricsOptions)
           (io.vertx.core.metrics MetricsOptions)))

(defn ^MetricsOptions get-metrics-options
  "Creates and returns a MetricsOptions object from the opts map.
  The map may contain a `:metrics-prefix` string that will be prepended to each metric,
  and a Dropwizard MetricRegistry instance under `:metric-registry`"
  [opts]
  (let [metrics-options (doto (DropwizardMetricsOptions.)
                          (.setEnabled true)
                          (.setJmxEnabled true)
                          (.setBaseName (:metrics-prefix opts "donkey")))]

    (if (:metric-registry opts)
      (.setMetricRegistry metrics-options (:metric-registry opts))
      (.setRegistryName metrics-options "donkey-registry"))
    metrics-options))
