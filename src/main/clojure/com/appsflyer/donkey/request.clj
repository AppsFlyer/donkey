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

(ns com.appsflyer.donkey.request
  (:require [com.appsflyer.donkey.result])
  (:import (clojure.lang IPersistentMap)
           (io.vertx.core Promise Future)
           (io.vertx.ext.web.client HttpRequest)
           (com.appsflyer.donkey FutureResult)
           (com.appsflyer.donkey.util TypeConverter)
           (com.appsflyer.donkey.client Client)))

(defprotocol Submittable
  (submit [this] [this body]
    "Submit an asynchronous request with an optional body. The body should be a
    string or a byte[].

    Returns a FutureResult that will be notified if the request succeeds or
    fails.")
  (submit-form [this body]
    "Submit an asynchronous request as `application/x-www-form-urlencoded`. A
    content-type header will be added to the request. If a `multipart/form-data`
    content-type header already exists it will be used instead.
    `body` is a map where all keys and values should be of type string.
    The body will be urlencoded when it's submitted.

    Returns a FutureResult that will be notified if the request succeeds or
    fails.")
  (submit-multipart-form [this body]
    "Submit an asynchronous request as `multipart/form-data`. A content-type
    header will be added to the request. You may use this function to send
    attributes and upload files.
    `body` is a map where all keys should be of type string. The values can be
    either string for simple attributes, or a map of file options when uploading
    a file.

    The file options map should include the following values:
    - filename: The name of the file, for example - image.png
    - pathname: The absolute path of the file.
      For example: /var/www/html/public/images/image.png
    - media-type: The MimeType of the file. For example - image/png
    - upload-as: Upload the file as `binary` or `text`. Default is `binary`

    Returns a FutureResult that will be notified if the request succeeds or
    fails."))


(declare submit-with-body)

(deftype AsyncRequest [^Client client ^HttpRequest req]
  Submittable
  (submit [_this]
    (FutureResult/create (.send client req)))

  (submit [_this body]
    (FutureResult/create ^Future (submit-with-body client req body)))

  (submit-form [_this body]
    (FutureResult/create
      (.sendForm client req (TypeConverter/toMultiMap ^IPersistentMap body))))

  (submit-multipart-form [_this body]
    (FutureResult/create
      (.sendMultiPartForm client req (TypeConverter/toMultipartForm ^IPersistentMap body)))))

(defn- handle-exception [^Throwable ex]
  (let [p (Promise/promise)]
    (.fail p ex)
    (.future p)))

(defn- submit-with-body [^Client client ^AsyncRequest req ^Object body]
  (try
    (.send client req (TypeConverter/toBuffer ^Object body))
    (catch Throwable ex
      (handle-exception ex))))
