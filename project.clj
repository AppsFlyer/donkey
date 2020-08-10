(def ^:private vertx-version "3.9.2")
(def ^:private junit-version "5.6.2")

(defproject donkey "0.1.0-SNAPSHOT"
  :description "Clojure Web Server and Client"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"
                       "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :target-path "target/%s"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[io.vertx/vertx-web ~vertx-version]
                 [io.vertx/vertx-web-client ~vertx-version]
                 [io.vertx/vertx-dropwizard-metrics ~vertx-version]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/spec.alpha "0.2.187"]]
  :junit ["test"]
  :profiles {:dev {:dependencies   [[org.clojure/clojure "1.10.1"]
                                    [io.vertx/vertx-junit5 ~vertx-version]
                                    [org.hamcrest/hamcrest-library "2.2"]
                                    [org.junit.jupiter/junit-jupiter ~junit-version]
                                    [org.mockito/mockito-junit-jupiter "3.4.6"]]
                   :plugins [[lein-junit "1.1.9"]]
                   :resource-paths ["test/resources"]}}
  :repl-options {:init-ns donkey.core})
