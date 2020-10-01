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
  (:import (com.appsflyer.donkey.server.ring.middleware JsonBodyDeserializer JsonBodySerializer)
           (com.fasterxml.jackson.databind ObjectMapper)
           (clojure.lang IFn)))

(defn ^IFn make-deserialize-middleware
  "Returns a middleware for deserializing request's body as JSON.
  An optional ObjectMapper may be supplied to customize the deserialization.
  By default all map keys will be turned into keywords"
  ([]
   (make-deserialize-middleware (jsonista/object-mapper {:decode-key-fn true})))
  ([^ObjectMapper mapper]
   (let [deserializer (JsonBodyDeserializer. mapper)]
     (fn [handler]
       (fn
         ([request]
          (handler (.handle deserializer request)))
         ([request respond raise]
          (try
            (handler (.handle deserializer request) respond raise)
            (catch Exception ex
              (raise ex)))))))))


(defn ^IFn make-serialize-middleware
  "Returns a middleware for serializing response's body as JSON.
  An optional ObjectMapper may be supplied to customize the serialization.
  By default all map keyword keys will be turned into strings"
  ([]
   (make-serialize-middleware (jsonista/object-mapper)))
  ([^ObjectMapper mapper]
   (let [serializer (JsonBodySerializer. mapper)]
     (fn [handler]
       (fn
         ([request]
          (.handle serializer (handler request)))
         ([request respond raise]
          (handler
            request
            (fn [response]
              (try
                (respond (.handle serializer response))
                (catch Exception ex
                  (raise ex))))
            raise)))))))
