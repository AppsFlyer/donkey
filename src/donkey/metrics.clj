(ns donkey.metrics
  (:import (io.vertx.ext.dropwizard DropwizardMetricsOptions Match MatchType)
           (io.vertx.core.metrics MetricsOptions)))

(defn ^MetricsOptions get-metrics-options
  ""
  [opts]
  (let [metrics-options (doto (DropwizardMetricsOptions.)
                          (.setEnabled true)
                          (.setJmxEnabled true)
                          (.setBaseName (:metrics-prefix opts "donkey"))
                          (.addMonitoredHttpServerUri
                            (doto (Match.)
                              (.setType MatchType/REGEX)
                              (.setValue "/.*")
                              (.setAlias "all"))))]

    (if (:metrics-registry opts)
      (.setMetricRegistry metrics-options (:metrics-registry opts))
      (.setRegistryName metrics-options "donkey-registry"))

    metrics-options))
