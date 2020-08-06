FROM clojure:openjdk-14-lein-2.9.3
WORKDIR /donkey
COPY src src
COPY project.clj project.clj
RUN lein uberjar
CMD ["java", "-server", "-Xms4G", "-Xmx4G", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-Dvertx.disableMetrics=true", "-Dvertx.threadChecks=false", "-Dvertx.disableContextTimings=true", "-Dvertx.disableTCCL=true", "-XX:+UseStringDeduplication", "-Djava.net.preferIPv4Stack=true", "-jar", "target/uberjar/donkey-0.1.0-standalone.jar"]
