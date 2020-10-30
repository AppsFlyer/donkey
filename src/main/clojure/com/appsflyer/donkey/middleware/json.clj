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
  (:require [jsonista.core :as jsonista]
            [com.appsflyer.donkey.middleware.base :as base])
  (:import (com.appsflyer.donkey.server.ring.middleware JsonBodyDeserializer JsonBodySerializer)
           (com.fasterxml.jackson.databind ObjectMapper)
           (clojure.lang IFn)))

(defn ^IFn make-deserialize-middleware
  "Returns a middleware for deserializing request's body as JSON.

  `opts` is an optional map with the following keys:
  - :mapper [ObjectMapper] Custom mapper that will be used for deserialization.
  - :ex-handler [fn] A function that will be called if an exception is thrown.
  It will be called with a map with the following keys:
  - :cause [Exception] The caught exception
  - :request [map] The request
  - :respond [fn] A function that should be called with the response. Only
  available in the 3 argument arity.
  - :raise [fn] A function that should be called with an exception. Only
  available in the 3 argument arity."
  ([]
   (make-deserialize-middleware {:mapper (jsonista/object-mapper {:decode-key-fn true})}))
  ([opts]
   (let [deserializer (JsonBodyDeserializer. ^ObjectMapper (:mapper opts))]
     (fn [handler]
       (base/make-ring-request-middleware {:middleware deserializer
                                           :handler    handler
                                           :ex-handler (:ex-handler opts)})))))


(defn ^IFn make-serialize-middleware
  "Returns a middleware for serializing response's body as JSON.

  `opts` is an optional map with the following keys:
  - :mapper [ObjectMapper] Custom mapper that will be used for serialization.
    By default all map keyword keys will be turned into strings
  - :ex-handler [fn] A function that will be called if an exception is thrown.
  It will be called with a map with the following keys:
  - :cause [Exception] The caught exception
  - :request [map] The request
  - :respond [fn] A function that should be called with the response. Only
  available in the 3 argument arity.
  - :raise [fn] A function that should be called with an exception. Only
  available in the 3 argument arity."
  ([]
   (make-serialize-middleware {:mapper (jsonista/object-mapper)}))
  ([opts]
   (let [serializer (JsonBodySerializer. ^ObjectMapper (:mapper opts))]
     (fn [handler]
       (base/make-ring-response-middleware {:middleware serializer
                                            :handler    handler
                                            :ex-handler (:ex-handler opts)})))))
