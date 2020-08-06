(ns donkey.metrics
  (:import (io.vertx.ext.dropwizard DropwizardMetricsOptions Match MatchType)
           (io.vertx.core.metrics MetricsOptions)))

(defn ^MetricsOptions get-metrics-options
  ""
  [opts]
  (let [metrics-options (doto (DropwizardMetricsOptions.)
                          (.setEnabled true)
                          (.setBaseName (:metrics-prefix opts "donkey"))
                          (.addMonitoredEventBusHandler
                            (-> (Match.) (.setType MatchType/REGEX) (.setValue "donkey.*")))
                          (.addMonitoredHttpServerUri
                            (-> (Match.) (.setType MatchType/REGEX) (.setValue "/*") (.setAlias "all"))))]

    (when (:jmx-enabled opts)
      (-> metrics-options
          (.setJmxEnabled true)
          (.setJmxDomain (:jmx-domain opts "localhost"))))

    (if (:metrics-registry opts)
      (.setMetricRegistry metrics-options (:metrics-registry opts))
      (.setRegistryName metrics-options "donkey-registry"))

    metrics-options))
