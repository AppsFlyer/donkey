(ns ^:integration com.appsflyer.donkey.server-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.set]
            [com.appsflyer.donkey.routes :as routes]
            [com.appsflyer.donkey.util :as util])
  (:import (io.vertx.ext.web.client HttpRequest)
           (clojure.lang ILookup)
           (io.vertx.core.json JsonObject)
           (io.vertx.core MultiMap)
           (io.netty.handler.codec.http HttpResponseStatus)
           (io.vertx.ext.web.multipart MultipartForm)
           (io.vertx.core.buffer Buffer)))


;; ---------- Initialization ---------- ;;

(def route-descriptors
  [routes/root-200
   routes/ring-spec
   routes/blocking-handler
   routes/single-path-variable
   routes/multi-path-variable
   routes/regex-path-variable
   routes/multi-regex-path-variable
   routes/blocking-middleware-handlers
   routes/async-middleware-handlers
   routes/blocking-exceptional-middleware-handlers
   routes/async-exceptional-middleware-handlers
   routes/explicit-consumes-json
   routes/explicit-consumes-multi-part-or-form-encoded-or-octet-stream])

(use-fixtures :once
              util/init-donkey
              (fn [test-fn] (util/init-donkey-server test-fn route-descriptors))
              util/init-web-client)

;; ---------- Tests ---------- ;;


(deftest test-basic-functionality
  (testing "the server should return a 200 response"
    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.get "/")
          (.send (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))))

(deftest test-ring-compliant-request
  (testing "it should include all the fields as specified by
  https://github.com/ring-clojure/ring/blob/master/SPEC"
    (let [response-promise (promise)]

      (-> util/vertx-client
          ^HttpRequest (.get (str "/ring-spec?foo=bar"))
          (.putHeader "DNT" "1")
          (.send (util/create-client-handler response-promise)))

      (let [^ILookup res (util/parse-response-body-when-resolved response-promise)]
        (is (= (:port util/default-server-options) (:server-port res)))
        (is (= (str "localhost:" (:port util/default-server-options)) (:server-name res)))
        (is (re-find #"127\.0\.0\.1:\d+" (:remote-addr res)))
        (is (= "/ring-spec" (:uri res)))
        (is (= "foo=bar" (:query-string res)))
        (is (= "bar" (-> res :query-params (get "foo"))))
        (is (= :http (:scheme res)))
        (is (= :get (:request-method res)))
        (is (= "HTTP/1.1" (:protocol res)))
        (is (= 3 (count
                   (clojure.set/intersection
                     #{"user-agent" "DNT" "host"}
                     (into #{} (keys (:headers res))))))))))

  (testing "it should include the raw and parsed query parameters"
    (let [query-string "foo=bar&count=6&valid=true&empty=false&version=4.0.9&ratio=2.4"
          response-promise (promise)]

      (-> util/vertx-client
          ^HttpRequest (.get (str "/ring-spec?" query-string))
          (.send (util/create-client-handler response-promise)))

      (let [res (util/parse-response-body-when-resolved response-promise)]
        (is (= query-string (:query-string res)))
        (is (= "bar" (-> res :query-params (get "foo"))))
        (is (= "6" (-> res :query-params (get "count"))))
        (is (= "true" (-> res :query-params (get "valid"))))
        (is (= "false" (-> res :query-params (get "empty"))))
        (is (= "4.0.9" (-> res :query-params (get "version"))))
        (is (= "2.4" (-> res :query-params (get "ratio"))))))))

(deftest path-variables-test
  (testing "it should parse path variables and includes them in the request"
    (testing "Single path variable"
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.post "/user/12345")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/parse-response-body-when-resolved response-promise)]
          (is (= {"id" "12345"} (:path-params res))))))

    (testing "Multiple path variables"
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.put "/user/joe-shm0e123/marketing")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/parse-response-body-when-resolved response-promise)]
          (is (= {"id" "joe-shm0e123" "department" "marketing"} (:path-params res))))))

    (testing "Single regex path variable with capturing group"
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get "/admin/909011")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "909011"} (:path-params res))))))

    (testing "Multiple regex path variables with capturing groups"
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get "/admin/10000/y-dept")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "10000" "param1" "y-dept"} (:path-params res))))))

    (testing "when regex path doesn't match the route is not called"
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get "/admin/1-123-4")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/NOT_FOUND) (.statusCode res)))))

      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get "/admin/1234/xyz-dept")
            (.send (util/create-client-handler response-promise)))

        (let [res (util/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/NOT_FOUND) (.statusCode res))))))))

(deftest blocking-handler-test
  (testing "it should call the 1 argument arity handler"
    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.get "/blocking-handler")
          (.send (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
        (is (= "hit /blocking-handler" (.bodyAsString res)))))))

(deftest middleware-per-route-test
  (testing "it should call each middleware with the result of the previous"
    (doseq [endpoint ["/route/middleware/blocking" "/route/middleware/async"]]
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get endpoint)
            (.send (util/create-client-handler response-promise)))

        (let [res (util/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
          (let [res-json (.bodyAsJsonObject res)]
            (is (= true (.getBoolean res-json "success")))
            (is (= 3 (.getInteger res-json "counter")))))))))

(deftest middleware-per-route-exception-test
  (testing "it should return an internal server error when an exception is thrown"
    (doseq [endpoint ["/route/middleware/blocking/exception" "/route/middleware/async/exception"]]
      (let [response-promise (promise)]
        (-> util/vertx-client
            ^HttpRequest (.get endpoint)
            (.send (util/create-client-handler response-promise)))

        (let [res (util/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/INTERNAL_SERVER_ERROR) (.statusCode res))))))))

(deftest test-consumes-content-type
  (testing "it should only accept requests with content type application/json"
    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/json")
          (.sendJsonObject (JsonObject. "{\"foo\":\"bar\"}")
                           (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/json")
          (.sendForm (-> (MultiMap/caseInsensitiveMultiMap) (.add "foo" "bar"))
                     (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (.statusCode res))))))

  (testing "it should accept requests with content type
  multipart/form-data, application/x-www-form-urlencoded, or application/octet-stream"
    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendMultipartForm (.attribute (MultipartForm/create) "foo" "bar")
                              (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendForm (-> (MultiMap/caseInsensitiveMultiMap) (.add "foo" "bar"))
                     (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.putHeader "content-type" "application/octet-stream")
          (.sendBuffer (Buffer/buffer "foo bar")
                       (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> util/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendJsonObject (JsonObject. "{\"foo\":\"bar\"}")
                           (util/create-client-handler response-promise)))

      (let [res (util/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (.statusCode res)))))))
