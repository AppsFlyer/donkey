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

(ns com.appsflyer.donkey.routes
  (:require [com.appsflyer.donkey.test-helper :as helper]
            [clojure.walk]))

(def glossary-with-keywords
  {:glossary
   {:title "example glossary"
    :id    49019246782
    :GlossDiv
           {:title "S"
            :GlossList
                   {:GlossEntry
                    {:ID        "SGML"
                     :GlossSee  "markup"
                     :Acronym   "SGML"
                     :SortAs    "SGML"
                     :GlossDef
                                {:para         "A meta-markup language, used to create markup languages such as DocBook."
                                 :GlossSeeAlso ["GML" "XML"]}
                     :GlossTerm "Standard Generalized Markup Language"
                     :Abbrev    "ISO 8879:1986"}}}}})

(def glossary-with-strings (clojure.walk/stringify-keys glossary-with-keywords))

(def sample-json-string
  "{\"glossary\":{\"title\":\"example glossary\",\"id\":49019246782,\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"Standard Generalized Markup Language\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO 8879:1986\",\"GlossDef\":{\"para\":\"A meta-markup language, used to create markup languages such as DocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}")

(defn return-request
  ([req]
   {:status 200
    :body   (-> (dissoc req :body)
                pr-str
                .getBytes)})
  ([req respond _raise]
   (respond
     {:status 200
      :body   (-> (dissoc req :body)
                  pr-str
                  .getBytes)})))

(defn async-return-request-handler
  "An asynchronous handler that returns the request (without the body)
   in the response body"
  [req respond _raise]
  (-> req return-request respond))

(defn serialize-body
  "Serializes the request body and returns it as the response body"
  ([req]
   {:status 200
    :body   (-> (:body req)
                pr-str
                .getBytes)})
  ([req respond _raise]
   (respond
     {:status 200
      :body   (-> (:body req)
                  pr-str
                  .getBytes)})))

(defn async-serialize-body-handler
  "An asynchronous handler that returns the request (without the body)
  in the response body"
  [req respond _raise]
  (-> req serialize-body respond))

(def root-200
  {:path         "/"
   :methods      [:get]
   :handler-mode :non-blocking
   :handler      (fn [_req respond _raise] (respond {:status 200}))})

(def echo-route
  {:path         "/echo"
   :handler-mode :blocking
   :handler      return-request})

(def echo-route-non-blocking
  {:path    "/echo/non-blocking"
   :handler return-request})

(def ring-spec
  {:path    "/ring-spec"
   :methods [:get]
   :handler async-return-request-handler})

(def single-path-variable
  {:path    "/user/:id"
   :methods [:post]
   :handler async-return-request-handler})

(def multi-path-variable
  {:path    "/user/:id/:department"
   :methods [:put]
   :handler async-return-request-handler})

(def regex-path-variable
  {:path       "/admin/(\\d+)"
   :methods    [:get]
   :match-type :regex
   :handler    async-return-request-handler})

(def multi-regex-path-variable
  {:path       "/admin/(\\d+)/([x-z]{1}-dept)"
   :methods    [:get]
   :match-type :regex
   :handler    async-return-request-handler})

(def blocking-handler
  {:path         "/blocking-handler"
   :methods      [:get]
   :handler-mode :blocking
   :handler      (fn [_req]
                   {:body "hit /blocking-handler"})})

(def explicit-consumes-json
  {:path         "/consumes/json"
   :methods      [:post]
   :consumes     ["application/json"]
   :handler-mode :blocking
   :handler      (fn [_req & _args] {})})

(def explicit-produces-json
  {:path         "/produces/json"
   :methods      [:get]
   :produces     ["application/json"]
   :handler-mode :blocking
   :handler      (fn [_req & _args] "{\"success\":true}")})

(def explicit-consumes-multi-part-or-form-encoded-or-octet-stream
  {:path         "/consumes/multi-urlencoded-stream"
   :methods      [:post]
   :consumes     ["multipart/form-data" "application/x-www-form-urlencoded" "application/octet-stream"]
   :handler-mode :blocking
   :handler      (fn [_req & _args] {})})

(def blocking-middleware-handlers
  {:path         "/route/middleware/blocking"
   :methods      [:get]
   :handler-mode :blocking
   :middleware   [(helper/make-pre-processing-middleware
                    #(assoc % :counter 1))
                  (helper/make-pre-processing-middleware
                    #(update % :counter inc))
                  (helper/make-pre-processing-middleware
                    #(update % :counter inc))
                  (helper/make-post-processing-middleware
                    (fn [res]
                      (update res :body #(str "{\"counter\":" (:counter %)
                                              ",\"success\":" (boolean (:success %)) "}"))))
                  (helper/make-post-processing-middleware
                    #(update-in % [:body :success] str "e"))
                  (helper/make-post-processing-middleware
                    #(update-in % [:body :success] str "u"))
                  (helper/make-post-processing-middleware
                    #(update-in % [:body :success] str "r"))
                  (helper/make-post-processing-middleware
                    #(update-in % [:body :success] str "t"))]
   :handler      (fn [req] {:status 200 :body {:counter (:counter req) :success ""}})})

(def non-blocking-middleware-handlers
  {:path         "/route/middleware/non-blocking"
   :methods      [:get]
   :handler-mode :non-blocking
   :middleware   [(helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (assoc req :counter 1))))
                  (helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (helper/make-post-processing-middleware
                    (fn [res respond _raise]
                      (respond (update res :body #(str "{\"counter\":" (:counter %)
                                                       ",\"success\":" (boolean (:success %)) "}")))))
                  (helper/make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "e"))))
                  (helper/make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "u"))))
                  (helper/make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "r"))))
                  (helper/make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "t"))))]
   :handler      (fn [req respond _raise] (respond {:status 200 :body {:counter (:counter req) :success ""}}))})


(def blocking-exceptional-middleware-handlers
  {:path         "/route/middleware/blocking/exception"
   :methods      [:get]
   :handler-mode :blocking
   :middleware   [(helper/make-pre-processing-middleware #(assoc % :counter 1))
                  (helper/make-pre-processing-middleware #(update % :counter inc))
                  (helper/make-pre-processing-middleware #(update % :counter str))
                  (helper/make-pre-processing-middleware #(update % :counter inc))
                  ; Should not be called
                  (helper/make-pre-processing-middleware #(update % :counter inc))]
   ; Should not be called
   :handler      (fn [req]
                   {:status 200 :body (-> req :counter str .getBytes)})})

(def non-blocking-exceptional-middleware-handlers
  {:path         "/route/middleware/non-blocking/exception"
   :methods      [:get]
   :handler-mode :non-blocking
   :middleware   [(helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (assoc req :counter 1))))
                  (helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (helper/make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter str))))
                  (helper/make-pre-processing-middleware
                    (fn [req respond raise]
                      (try
                        (respond (update req :counter inc))
                        (catch Exception ex
                          (raise ex)))))]
   ; Should not be called
   :handler      (fn [_req respond _raise] (respond {:status 200}))})

(def serialize-body-route
  {:path         "/serialize-body"
   :handler-mode :blocking
   :handler      serialize-body})

(def serialize-body-non-blocking-route
  {:path    "/serialize-body/non-blocking"
   :handler async-serialize-body-handler})

(def json-response
  {:path         "/json"
   :handler-mode :blocking
   :handler      (fn [_req] {:status 200 :body glossary-with-keywords})})
