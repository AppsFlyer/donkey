(ns ^:integration com.appsflyer.donkey.server-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.set]
            [com.appsflyer.donkey.routes :as routes]
            [com.appsflyer.donkey.test-helper :as helper])
  (:import (clojure.lang ILookup)
           (io.netty.handler.codec.http HttpResponseStatus)
           (io.vertx.core.json JsonObject)
           (io.vertx.core MultiMap)
           (io.vertx.ext.web.client HttpRequest)
           (io.vertx.ext.web.multipart MultipartForm)
           (io.vertx.core.buffer Buffer)))


;; ---------- Initialization ---------- ;;

(def route-descriptors
  [routes/root-200
   routes/ring-spec
   routes/echo-route
   routes/blocking-handler
   routes/single-path-variable
   routes/multi-path-variable
   routes/regex-path-variable
   routes/multi-regex-path-variable
   routes/blocking-middleware-handlers
   routes/non-blocking-middleware-handlers
   routes/blocking-exceptional-middleware-handlers
   routes/non-blocking-exceptional-middleware-handlers
   routes/explicit-consumes-json
   routes/explicit-consumes-multi-part-or-form-encoded-or-octet-stream])

(use-fixtures :once
              helper/init-donkey
              (fn [test-fn] (helper/init-donkey-server test-fn route-descriptors))
              helper/init-web-client)

;; ---------- Tests ---------- ;;


(deftest test-basic-functionality
  (testing "the server should return a 200 response"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get "/")
          (.send (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))))

(deftest test-ring-compliant-request
  (testing "it should include all the fields as specified by
  https://github.com/ring-clojure/ring/blob/master/SPEC"
    (let [response-promise (promise)]

      (-> helper/vertx-client
          ^HttpRequest (.get (str "/ring-spec?foo=bar"))
          (.putHeader "DNT" "1")
          (.send (helper/create-client-handler response-promise)))

      (let [^ILookup res (helper/parse-response-body-when-resolved response-promise)]
        (is (= (:port helper/default-server-options) (:server-port res)))
        (is (= (str "localhost:" (:port helper/default-server-options)) (:server-name res)))
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

      (-> helper/vertx-client
          ^HttpRequest (.get (str "/ring-spec?" query-string))
          (.send (helper/create-client-handler response-promise)))

      (let [res (helper/parse-response-body-when-resolved response-promise)]
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
        (-> helper/vertx-client
            ^HttpRequest (.post "/user/12345")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/parse-response-body-when-resolved response-promise)]
          (is (= {"id" "12345"} (:path-params res))))))

    (testing "Multiple path variables"
      (let [response-promise (promise)]
        (-> helper/vertx-client
            ^HttpRequest (.put "/user/joe-shm0e123/marketing")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/parse-response-body-when-resolved response-promise)]
          (is (= {"id" "joe-shm0e123" "department" "marketing"} (:path-params res))))))

    (testing "Single regex path variable with capturing group"
      (let [response-promise (promise)]
        (-> helper/vertx-client
            ^HttpRequest (.get "/admin/909011")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "909011"} (:path-params res))))))

    (testing "Multiple regex path variables with capturing groups"
      (let [response-promise (promise)]
        (-> helper/vertx-client
            ^HttpRequest (.get "/admin/10000/y-dept")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "10000" "param1" "y-dept"} (:path-params res))))))

    (testing "when regex path doesn't match the route is not called"
      (let [response-promise (promise)]
        (-> helper/vertx-client
            ^HttpRequest (.get "/admin/1-123-4")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/NOT_FOUND) (.statusCode res)))))

      (let [response-promise (promise)]
        (-> helper/vertx-client
            ^HttpRequest (.get "/admin/1234/xyz-dept")
            (.send (helper/create-client-handler response-promise)))

        (let [res (helper/wait-for-response response-promise)]
          (is (= (.code HttpResponseStatus/NOT_FOUND) (.statusCode res))))))))

(deftest blocking-handler-test
  (testing "it should call the 1 argument arity handler"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.get "/blocking-handler")
          (.send (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))
        (is (= "hit /blocking-handler" (.bodyAsString res)))))))

(deftest test-consumes-content-type
  (testing "it should only accept requests with content type application/json"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/json")
          (.sendJsonObject (JsonObject. "{\"foo\":\"bar\"}")
                           (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/json")
          (.sendForm (-> (MultiMap/caseInsensitiveMultiMap) (.add "foo" "bar"))
                     (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (.statusCode res))))))

  (testing "it should accept requests with content type
  multipart/form-data, application/x-www-form-urlencoded, or application/octet-stream"
    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendMultipartForm (.attribute (MultipartForm/create) "foo" "bar")
                              (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendForm (-> (MultiMap/caseInsensitiveMultiMap) (.add "foo" "bar"))
                     (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.putHeader "content-type" "application/octet-stream")
          (.sendBuffer (Buffer/buffer "foo bar")
                       (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/OK) (.statusCode res)))))

    (let [response-promise (promise)]
      (-> helper/vertx-client
          ^HttpRequest (.post "/consumes/multi-urlencoded-stream")
          (.sendJsonObject (JsonObject. "{\"foo\":\"bar\"}")
                           (helper/create-client-handler response-promise)))

      (let [res (helper/wait-for-response response-promise)]
        (is (= (.code HttpResponseStatus/UNSUPPORTED_MEDIA_TYPE) (.statusCode res)))))))

(deftest test-post-requests
  (testing "Multi-part POST request should include :form-params"
    (let [response-promise (promise)
          body (-> (MultipartForm/create) (.attribute "foo" "bar"))]
      (-> helper/vertx-client
          ^HttpRequest (.post "/echo")
          (.sendMultipartForm body (helper/create-client-handler response-promise)))

      (let [^ILookup res (helper/parse-response-body-when-resolved response-promise)]
        (is (= "bar" (get-in res [:form-params "foo"]))))))

  (testing "x-www-form-urlencoded POST request should include :form-params"
    (let [response-promise (promise)
          body (-> (MultiMap/caseInsensitiveMultiMap) (.add "foo" "bar"))]
      (-> helper/vertx-client
          ^HttpRequest (.post "/echo")
          (.sendForm body (helper/create-client-handler response-promise)))
      (let [^ILookup res (helper/parse-response-body-when-resolved response-promise)]
        (is (= "bar" (get-in res [:form-params "foo"])))))))
