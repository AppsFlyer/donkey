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

(def ^:private vertx-version "3.9.2")
(def ^:private junit-version "5.6.2")

(defproject donkey "0.1.0-SNAPSHOT"
  :description "Clojure Web Server and Client"
  :url "https://github.com/AppsFlyer/donkey"
  :license {:name "APACHE LICENSE, VERSION 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure" "src/test/java"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :target-path "target/%s"
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"
                       "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"
                       "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
                       "-Dvertx.threadChecks=false"
                       "-Dvertx.disableContextTimings=true"]
  :global-vars {*warn-on-reflection* true}
  :javac-options ["-target" "11" "-source" "11"]
  :jar-exclusions [#"^.java"]
  :dependencies [[io.vertx/vertx-web ~vertx-version]
                 [io.vertx/vertx-web-client ~vertx-version]
                 [io.vertx/vertx-dropwizard-metrics ~vertx-version]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/spec.alpha "0.2.187"]
                 [metosin/jsonista "0.2.7"]
                 [org.jetbrains/annotations "13.0" :scope "compile"]]
  :profiles {:dev     {:dependencies   [[org.clojure/tools.logging "1.1.0"]
                                        [ch.qos.logback/logback-classic "1.2.3"]
                                        [io.vertx/vertx-junit5 ~vertx-version]
                                        [org.hamcrest/hamcrest-library "2.2"]
                                        [org.junit.jupiter/junit-jupiter ~junit-version]
                                        [org.mockito/mockito-junit-jupiter "3.4.6"]
                                        [criterium "0.4.6"]
                                        [clj-http "3.10.2"]]
                       :resource-paths ["src/test/resources"]
                       :jvm-opts       ^:replace ["-Dvertx.threadChecks=false"
                                                  "-Dvertx.disableContextTimings=false"]
                       :plugins        [[lein-kibit "0.1.8"]
                                        [lein-cloverage "1.1.2"]]}
             :uberjar {:aot :all}}
  :aliases {"coveralls" ["cloverage" "--junit" "--output" "target/coveralls/clojure" "--coveralls"]}
  :pom-location "target/"
  :repl-options {:init-ns com.appsflyer.donkey.core})
