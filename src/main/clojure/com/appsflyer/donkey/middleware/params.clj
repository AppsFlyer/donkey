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

(ns com.appsflyer.donkey.middleware.params
  (:require [com.appsflyer.donkey.middleware.base :as base])
  (:import (com.appsflyer.donkey.server.ring.middleware
             QueryParamsParser
             FormParamsKeywordizer
             FormParamsKeywordizer$Options QueryParamsParser$Options)))

(defn parse-query-params
  "Parses the request's `query-string` into a map of `query-params`.
  `opts` is an optional map with the following keys:
  - :keywordize [boolean] Whether query params keys should be turned into
      keywords.
  - :ex-handler [fn] A function that will be called if an exception is thrown.
    It will be called with a map with the following keys:
      - :cause [Exception] The caught exception
      - :request [map] The request
      - :respond [fn] A function that should be called with the response. Only
        available in the 3 argument arity.
      - :raise [fn] A function that should be called with an exception. Only
        available in the 3 argument arity."
  ([] (parse-query-params nil))
  ([{:keys [keywordize ex-handler] :or {keywordize false}}]
   (fn [handler]
     (base/make-ring-request-middleware
       {:middleware (QueryParamsParser.
                      (doto
                        (QueryParamsParser$Options.)
                        (.keywordizeKeys (boolean keywordize))))
        :handler    handler
        :ex-handler ex-handler}))))

(defn keywordize-form-params
  "Parses the request's `body` into a map of `form-params`.
  `opts` is an optional map with the following keys:
  - :ex-handler [fn] A function that will be called if an exception is thrown.
    It will be called with a map with the following keys:
      - :cause [Exception] The caught exception
      - :request [map] The request
      - :respond [fn] A function that should be called with the response. Only
        available in the 3 argument arity.
      - :raise [fn] A function that should be called with an exception. Only
        available in the 3 argument arity."
  ([] (keywordize-form-params {:deep true}))
  ([opts]
   (fn [handler]
     (base/make-ring-request-middleware
       {:middleware (FormParamsKeywordizer. (FormParamsKeywordizer$Options. (:deep opts)))
        :handler    handler
        :ex-handler (:ex-handler opts)}))))
