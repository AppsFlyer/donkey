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

(ns com.appsflyer.donkey.middleware.json-test
  (:require [clojure.test :refer [deftest testing is]]
            [com.appsflyer.donkey.middleware.json :refer [make-deserialize-middleware
                                                          make-serialize-middleware]]
            [com.appsflyer.donkey.test-helper :as helper]
            [com.appsflyer.donkey.routes :as routes]
            [jsonista.core :as jsonista])
  (:import (io.vertx.ext.web.client HttpRequest HttpResponse)
           (io.vertx.core.buffer Buffer)
           (io.netty.handler.codec.http HttpResponseStatus)))

(defn- make-request [uri ^String body response-promise]
  (-> ^HttpRequest (.post helper/vertx-client uri)
      (.sendBuffer
        (Buffer/buffer body)
        (helper/create-client-handler response-promise)))
  response-promise)

(defn- execute-parse-body-test [uri]
  (helper/parse-response-body-when-resolved
    (make-request uri routes/sample-json-string (promise))))

(deftest parse-body-with-default-mapper-test
  (testing "it should parse the request body as json. When not supplying an
  ObjectMapper, all the keys should be keywords."
    (let [-routes [routes/serialize-body-route routes/serialize-body-non-blocking-route]]
      (helper/run-with-server-and-client
        (fn []
          (doseq [path (mapv :path -routes)]
            (let [res (execute-parse-body-test path)]
              (is (= routes/sample-json res)))))
        -routes
        [(make-deserialize-middleware)]))))

(deftest parse-body-with-mapper-test
  (testing "it should parse the request body as json using the supplied ObjectMapper"
    (let [-routes [routes/serialize-body-route routes/serialize-body-non-blocking-route]]
      (helper/run-with-server-and-client
        (fn []
          (doseq [path (mapv :path -routes)]
            (let [res (execute-parse-body-test path)]
              (is (map? (get res "glossary")))
              (is (= (get-in res ["glossary" "title"]) "example glossary"))
              (is (= (get-in res ["glossary" "id"]) 49019246782))
              (is (map? (get-in res ["glossary" "GlossDiv"])))
              (is (= (get-in res ["glossary" "GlossDiv" "title"]) "S"))
              (is (map? (get-in res ["glossary" "GlossDiv" "GlossList"])))
              (is (map? (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry"])))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "ID"]) "SGML"))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "SortAs"]) "SGML"))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossTerm"])
                     "Standard Generalized Markup Language"))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "Acronym"]) "SGML"))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "Abbrev"]) "ISO 8879:1986"))
              (is (map? (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossDef"])))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossDef" "para"])
                     "A meta-markup language, used to create markup languages such as DocBook."))
              (is (vector (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossDef" "GlossSeeAlso"])))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossDef" "GlossSeeAlso"])
                     ["GML" "XML"]))
              (is (= (get-in res ["glossary" "GlossDiv" "GlossList" "GlossEntry" "GlossSee"]) "markup")))))
        -routes
        [(make-deserialize-middleware (jsonista/object-mapper))]))))

(deftest invalid-json-test
  (testing "it should return a 400 Bad Request when `body` cannot be parsed as json"
    (let [-routes [routes/serialize-body-route routes/serialize-body-non-blocking-route]]
      (helper/run-with-server-and-client
        (fn []
          (doseq [path (mapv :path -routes)]
            (let [^HttpResponse res (helper/wait-for-response
                                      (make-request path "invalid json" (promise)))]
              (is (= (.code HttpResponseStatus/BAD_REQUEST) (.statusCode res)))
              (is (= (.reasonPhrase HttpResponseStatus/BAD_REQUEST) (.statusMessage res))))))
        -routes
        [(make-deserialize-middleware)]))))

(deftest serialize-body-with-default-mapper-test
  (testing "it should return a response where the body is a Clojure map
   serialized into json. "
    (helper/run-with-server-and-client
      (fn []
        (let [^HttpResponse res (helper/wait-for-response
                                  (make-request (:path routes/json-response) "" (promise)))
              body (jsonista/read-value
                     (.bodyAsString res)
                     (jsonista/object-mapper {:decode-key-fn true}))]
          (is (= routes/sample-json body))))
      ; The json-response route just sends back `sample-json` in the response body
      [routes/json-response]
      [(make-serialize-middleware)])))
