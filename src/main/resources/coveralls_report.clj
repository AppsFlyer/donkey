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

(ns coveralls-report
  (:gen-class)
  (:require [jsonista.core :as json]))

(def ^:private ^:const clj-report-path "target/coveralls/clojure/coveralls.json")
(def ^:private ^:const java-report-path "target/coveralls/java/coveralls.json")
(def ^:private ^:const final-report-path "target/coveralls/coveralls.json")

(defn- get-report [path]
  (println (str "Parsing coveralls report at " path))
  (let [report (slurp path)]
    (when (not report)
      (throw (ex-info (str "cannot find coveralls report at " path) {})))
    (json/read-value report)))

(defn -main [& _]
  (let [clj-report (get-report clj-report-path)
        java-report (get-report java-report-path)
        final-report (update java-report "source_files" concat (get clj-report "source_files"))]
    (println (str "Writing final coveralls report to " final-report-path))
    (spit final-report-path (json/write-value-as-string final-report))))
