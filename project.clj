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

(def ^:private ^:const vertx-version "4.0.0")
(def ^:private ^:const junit-version "5.7.0")
(def ^:private ^:const clojure-lang-version "1.10.1")
(def ^:private ^:const clojure-spec-version "0.2.187")
(def ^:private ^:const clojure-logging-version "1.1.0")
(def ^:private ^:const jsonista-version "0.3.0")
(def ^:private ^:const slf4j-version "1.7.30")
(def ^:private ^:const logback-version "1.2.3")
(def ^:private ^:const hamcrest-version "2.2")
(def ^:private ^:const mockito-version "3.6.28")
(def ^:private ^:const jetbrains-version "20.1.0")
(def ^:private ^:const ring-core-version "1.8.2")
(def ^:private ^:const ring-json-version "0.5.0")
(def ^:private ^:const criterium-version "0.4.6")

(defproject com.appsflyer/donkey "0.5.0-SNAPSHOT"
  :description "Clojure Server and Client"
  :url "https://github.com/AppsFlyer/donkey"
  :license {:name "APACHE LICENSE, VERSION 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure" "src/test/java"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :target-path "target/%s"
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"]
  :global-vars {*warn-on-reflection* true}
  :javac-options ["-target" "11" "-source" "11"]
  :jar-exclusions [#"^.java"]
  :dependencies [[io.vertx/vertx-web ~vertx-version]
                 [io.vertx/vertx-web-client ~vertx-version]
                 [io.vertx/vertx-dropwizard-metrics ~vertx-version]
                 [org.slf4j/slf4j-api ~slf4j-version]
                 [org.clojure/clojure ~clojure-lang-version]
                 [org.clojure/spec.alpha ~clojure-spec-version]
                 [metosin/jsonista ~jsonista-version]
                 [org.jetbrains/annotations ~jetbrains-version]]
  :profiles {:dev       {:dependencies   [[org.clojure/tools.logging ~clojure-logging-version]
                                          [io.vertx/vertx-junit5 ~vertx-version]
                                          [org.hamcrest/hamcrest-library ~hamcrest-version]
                                          [org.junit.jupiter/junit-jupiter ~junit-version]
                                          [org.mockito/mockito-junit-jupiter ~mockito-version]
                                          [ch.qos.logback/logback-classic ~logback-version :scope "provided"]
                                          [criterium ~criterium-version :scope "provided"]
                                          [ring/ring-core ~ring-core-version :scope "provided"]
                                          [ring/ring-json ~ring-json-version :scope "provided"]]
                         :resource-paths ["src/test/resources"]
                         :plugins        [[lein-kibit "0.1.8"]
                                          [lein-cloverage "1.1.2"]]}
             :benchmark {:dependencies [[ch.qos.logback/logback-classic ~logback-version]
                                        [criterium ~criterium-version]
                                        [ring/ring-core ~ring-core-version]
                                        [ring/ring-json ~ring-json-version]]}
             :uberjar   {:aot :all}}
  :aliases {"coveralls" ["cloverage" "--junit" "--no-html" "--output" "target/coveralls/clojure" "--coveralls"]}
  :pom-location "target/"
  :repl-options {:init-ns com.appsflyer.donkey.core})
