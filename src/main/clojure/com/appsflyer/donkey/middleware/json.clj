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

(ns com.appsflyer.donkey.middleware.json
  (:require [jsonista.core :as jsonista])
  (:import (com.appsflyer.donkey.server.ring.middleware JsonBodyParser)
           (com.fasterxml.jackson.databind ObjectMapper)
           (clojure.lang IFn)))

(defn ^IFn make-deserialize-middleware
  "Returns a middleware for parsing requests body as JSON.
  An optional ObjectMapper can be supplied to customize the deserialization.
  By default all map keys will be turned into keywords"
  ([]
   (make-deserialize-middleware (jsonista/object-mapper {:decode-key-fn true})))
  ([^ObjectMapper mapper]
   (fn [handler]
     (fn
       ([request]
        (handler (-> (JsonBodyParser. mapper) (.handle request))))
       ([request respond raise]
        (handler (-> (JsonBodyParser. mapper) (.handle request)) respond raise))))))
