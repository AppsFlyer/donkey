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

(ns com.appsflyer.donkey.benchmark
  (:gen-class)
  (:require [criterium.core :as cc]
            [ring.core.protocols :refer [write-body-to-stream]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [com.appsflyer.donkey.middleware.params :refer [parse-query-params]]
            [com.appsflyer.donkey.middleware.json :refer [make-serialize-middleware
                                                          make-deserialize-middleware]]
            [jsonista.core :as jsonista]))

(declare -main)

(def ^:private ^:const query-str
  "foo=bar&city=New%20York&occupation=Shop%20Keeper&age=49&name=Greg%20Joy&duration=10000")

(def ^:private json-body
  (.getBytes "[{\"id\":1,\"name\":\"Leanne Graham\",\"username\":\"Bret\",\"email\":\"Sincere@april.biz\",\"address\":{\"street\":\"Kulas Light\",\"suite\":\"Apt. 556\",\"city\":\"Gwenborough\",\"zipcode\":\"92998-3874\",\"geo\":{\"lat\":\"-37.3159\",\"lng\":\"81.1496\"}},\"phone\":\"1-770-736-8031 x56442\",\"website\":\"hildegard.org\",\"company\":{\"name\":\"Romaguera-Crona\",\"catchPhrase\":\"Multi-layered client-server neural-net\",\"bs\":\"harness real-time e-markets\"}},{\"id\":2,\"name\":\"Ervin Howell\",\"username\":\"Antonette\",\"email\":\"Shanna@melissa.tv\",\"address\":{\"street\":\"Victor Plains\",\"suite\":\"Suite 879\",\"city\":\"Wisokyburgh\",\"zipcode\":\"90566-7771\",\"geo\":{\"lat\":\"-43.9509\",\"lng\":\"-34.4618\"}},\"phone\":\"010-692-6593 x09125\",\"website\":\"anastasia.net\",\"company\":{\"name\":\"Deckow-Crist\",\"catchPhrase\":\"Proactive didactic contingency\",\"bs\":\"synergize scalable supply-chains\"}},{\"id\":3,\"name\":\"Clementine Bauch\",\"username\":\"Samantha\",\"email\":\"Nathan@yesenia.net\",\"address\":{\"street\":\"Douglas Extension\",\"suite\":\"Suite 847\",\"city\":\"McKenziehaven\",\"zipcode\":\"59590-4157\",\"geo\":{\"lat\":\"-68.6102\",\"lng\":\"-47.0653\"}},\"phone\":\"1-463-123-4447\",\"website\":\"ramiro.info\",\"company\":{\"name\":\"Romaguera-Jacobson\",\"catchPhrase\":\"Face to face bifurcated interface\",\"bs\":\"e-enable strategic applications\"}},{\"id\":4,\"name\":\"Patricia Lebsack\",\"username\":\"Karianne\",\"email\":\"Julianne.OConner@kory.org\",\"address\":{\"street\":\"Hoeger Mall\",\"suite\":\"Apt. 692\",\"city\":\"South Elvis\",\"zipcode\":\"53919-4257\",\"geo\":{\"lat\":\"29.4572\",\"lng\":\"-164.2990\"}},\"phone\":\"493-170-9623 x156\",\"website\":\"kale.biz\",\"company\":{\"name\":\"Robel-Corkery\",\"catchPhrase\":\"Multi-tiered zero tolerance productivity\",\"bs\":\"transition cutting-edge web services\"}},{\"id\":5,\"name\":\"Chelsey Dietrich\",\"username\":\"Kamren\",\"email\":\"Lucio_Hettinger@annie.ca\",\"address\":{\"street\":\"Skiles Walks\",\"suite\":\"Suite 351\",\"city\":\"Roscoeview\",\"zipcode\":\"33263\",\"geo\":{\"lat\":\"-31.8129\",\"lng\":\"62.5342\"}},\"phone\":\"(254)954-1289\",\"website\":\"demarco.info\",\"company\":{\"name\":\"Keebler LLC\",\"catchPhrase\":\"User-centric fault-tolerant solution\",\"bs\":\"revolutionize end-to-end systems\"}},{\"id\":6,\"name\":\"Mrs. Dennis Schulist\",\"username\":\"Leopoldo_Corkery\",\"email\":\"Karley_Dach@jasper.info\",\"address\":{\"street\":\"Norberto Crossing\",\"suite\":\"Apt. 950\",\"city\":\"South Christy\",\"zipcode\":\"23505-1337\",\"geo\":{\"lat\":\"-71.4197\",\"lng\":\"71.7478\"}},\"phone\":\"1-477-935-8478 x6430\",\"website\":\"ola.org\",\"company\":{\"name\":\"Considine-Lockman\",\"catchPhrase\":\"Synchronised bottom-line interface\",\"bs\":\"e-enable innovative applications\"}},{\"id\":7,\"name\":\"Kurtis Weissnat\",\"username\":\"Elwyn.Skiles\",\"email\":\"Telly.Hoeger@billy.biz\",\"address\":{\"street\":\"Rex Trail\",\"suite\":\"Suite 280\",\"city\":\"Howemouth\",\"zipcode\":\"58804-1099\",\"geo\":{\"lat\":\"24.8918\",\"lng\":\"21.8984\"}},\"phone\":\"210.067.6132\",\"website\":\"elvis.io\",\"company\":{\"name\":\"Johns Group\",\"catchPhrase\":\"Configurable multimedia task-force\",\"bs\":\"generate enterprise e-tailers\"}},{\"id\":8,\"name\":\"Nicholas Runolfsdottir V\",\"username\":\"Maxime_Nienow\",\"email\":\"Sherwood@rosamond.me\",\"address\":{\"street\":\"Ellsworth Summit\",\"suite\":\"Suite 729\",\"city\":\"Aliyaview\",\"zipcode\":\"45169\",\"geo\":{\"lat\":\"-14.3990\",\"lng\":\"-120.7677\"}},\"phone\":\"586.493.6943 x140\",\"website\":\"jacynthe.com\",\"company\":{\"name\":\"Abernathy Group\",\"catchPhrase\":\"Implemented secondary concept\",\"bs\":\"e-enable extensible e-tailers\"}},{\"id\":9,\"name\":\"Glenna Reichert\",\"username\":\"Delphine\",\"email\":\"Chaim_McDermott@dana.io\",\"address\":{\"street\":\"Dayna Park\",\"suite\":\"Suite 449\",\"city\":\"Bartholomebury\",\"zipcode\":\"76495-3109\",\"geo\":{\"lat\":\"24.6463\",\"lng\":\"-168.8889\"}},\"phone\":\"(775)976-6794 x41206\",\"website\":\"conrad.com\",\"company\":{\"name\":\"Yost and Sons\",\"catchPhrase\":\"Switchable contextually-based project\",\"bs\":\"aggregate real-time technologies\"}},{\"id\":10,\"name\":\"Clementina DuBuque\",\"username\":\"Moriah.Stanton\",\"email\":\"Rey.Padberg@karina.biz\",\"address\":{\"street\":\"Kattie Turnpike\",\"suite\":\"Suite 198\",\"city\":\"Lebsackbury\",\"zipcode\":\"31428-2261\",\"geo\":{\"lat\":\"-38.2386\",\"lng\":\"57.2232\"}},\"phone\":\"024-648-3804\",\"website\":\"ambrose.net\",\"company\":{\"name\":\"Hoeger LLC\",\"catchPhrase\":\"Centralized empowering task-force\",\"bs\":\"target end-to-end models\"}}]"))

(def ^:private json-response (jsonista/read-value json-body))

(def ^:private my-ns (-> (meta #'-main) :ns str))

(defmacro title [benchmark]
  (let [benchmark# (symbol my-ns (str benchmark))]
    `(println (str "Running benchmark for '" (:name (meta (var ~benchmark#))) "' ..."))))

;Evaluation count : 8527140 in 60 samples of 142119 calls.
;Execution time mean : 7.189748 µs
;Execution time std-deviation : 304.464220 ns
;Execution time lower quantile : 6.951543 µs ( 2.5%)
;Execution time upper quantile : 7.943209 µs (97.5%)
;Overhead used : 7.063382 ns
(defn bench-ring-wrap-keyword-params
  ([] (bench-ring-wrap-keyword-params query-str))
  ([query-str]
   (title bench-ring-wrap-keyword-params)

   (let [middleware ((comp wrap-params wrap-keyword-params) identity)]
     (cc/bench (middleware {:query-string query-str})))))

; =============================================================

;Evaluation count : 60650820 in 60 samples of 1010847 calls.
;Execution time mean : 986.521454 ns
;Execution time std-deviation : 11.881765 ns
;Execution time lower quantile : 977.748967 ns ( 2.5%)
;Execution time upper quantile : 1.024412 µs (97.5%)
;Overhead used : 7.063382 ns
; ==== 7.7x faster ===
(defn bench-donkey-keywordize-query-params
  ([] (bench-donkey-keywordize-query-params query-str))
  ([query-str]
   (title bench-donkey-keywordize-query-params)

   (let [middleware ((parse-query-params {:keywordize true}) identity)]
     (cc/bench (middleware {:query-string query-str})))))

; =============================================================

;Evaluation count : 1937520 in 60 samples of 32292 calls.
;Execution time mean : 31.168359 µs
;Execution time std-deviation : 423.077800 ns
;Execution time lower quantile : 30.818259 µs ( 2.5%)
;Execution time upper quantile : 31.706660 µs (97.5%)
;Overhead used : 7.063382 ns
(defn bench-ring-wrap-json-body
  ([] (bench-ring-wrap-json-body json-body))
  ([json-body]
   (title bench-ring-wrap-json-body)

   (let [middleware (wrap-json-body identity {:keywords? true})]
     (cc/bench
       (doall                                               ;Returns a lazy sequence - so it has to be materialized for benchmark
         (:body (middleware {:headers {"content-type" "application/json"}
                             :body    json-body})))))))

; =============================================================

;Evaluation count : 2892120 in 60 samples of 48202 calls.
;Execution time mean : 20.892834 µs
;Execution time std-deviation : 309.209309 ns
;Execution time lower quantile : 20.686347 µs ( 2.5%)
;Execution time upper quantile : 21.677851 µs (97.5%)
;Overhead used : 1.986951 ns
; ==== 35-54% faster ===
(defn bench-donkey-parse-json-body
  ([] (bench-donkey-parse-json-body json-body))
  ([json-body]
   (title bench-donkey-parse-json-body)

   (let [middleware ((make-deserialize-middleware) identity)]
     (cc/bench (middleware {:body json-body})))))

; =============================================================

;Evaluation count : 1703400 in 60 samples of 28390 calls.
;Execution time mean : 35.423383 µs
;Execution time std-deviation : 425.040995 ns
;Execution time lower quantile : 35.093443 µs ( 2.5%)
;Execution time upper quantile : 36.464090 µs (97.5%)
;Overhead used : 7.063382 ns
(defn bench-ring-wrap-json-response
  ([] (bench-ring-wrap-json-response json-response))
  ([json-response]
   (title bench-ring-wrap-json-response)

   (let [middleware (wrap-json-response identity)]
     (cc/bench
       (:body (middleware {:headers {"Content-Type" "application/json"}
                           :body    json-response}))))))

; =============================================================

;Evaluation count : 4799340 in 60 samples of 79989 calls.
;Execution time mean : 12.567366 µs
;Execution time std-deviation : 103.403297 ns
;Execution time lower quantile : 12.474557 µs ( 2.5%)
;Execution time upper quantile : 12.789686 µs (97.5%)
;Overhead used : 7.063382 ns
; ==== 2.85x faster ===
(defn bench-donkey-serialize-json-response
  ([] (bench-donkey-serialize-json-response json-response))
  ([json-response]
   (title bench-donkey-serialize-json-response)

   (let [middleware ((make-serialize-middleware) identity)]
     (cc/bench (String. ^bytes (:body (middleware {:body json-response})))))))

; =============================================================

(defn- run-all []
  (bench-ring-wrap-keyword-params query-str)
  (bench-donkey-keywordize-query-params query-str)
  (bench-ring-wrap-json-body json-body)
  (bench-donkey-parse-json-body json-body)
  (bench-ring-wrap-json-response json-response)
  (bench-donkey-serialize-json-response json-response))

(defn -main [& args]
  (let [ran (atom false)]
    (doseq [fn-name args]
      (when-let [func (resolve (symbol my-ns (str fn-name)))]
        (reset! ran true)
        (func)))

    (when (not @ran)
      (println "Running all benchmarks")
      (run-all))))
