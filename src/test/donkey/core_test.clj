(ns ^:integration donkey.core-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [donkey.core :as donkey]
            [donkey.server :as server])
  (:import (io.vertx.ext.web.client WebClient WebClientOptions HttpResponse HttpRequest)
           (io.vertx.core Vertx Handler)
           (donkey.server DonkeyServer)
           (io.vertx.core Future)
           (com.appsflyer.donkey.server Server)
           (clojure.lang ILookup)))

(defn- return-request [req]
  {:status 200
   :body   (-> (dissoc req :body)
               pr-str
               .getBytes)})

(defn- async-return-request-handler
  "An asynchronous handler that returns the request in the response body"
  [req respond _raise]
  (-> req return-request respond))

;; ---------- Route Definitions ---------- ;;

(def root-200 {:path         "/"
               :methods      [:get]
               :handler-mode :non-blocking
               :handlers     [(fn [_req respond _raise] (respond {:status 200}))]})

(def ring-spec {:path     "/ring-spec"
                :methods  [:get]
                :handlers [async-return-request-handler]})

(def single-path-variable {:path     "/user/:id"
                           :methods  [:post]
                           :handlers [async-return-request-handler]})

(def multi-path-variable {:path     "/user/:id/:department"
                          :methods  [:put]
                          :handlers [async-return-request-handler]})

(def regex-path-variable {:path       "/admin/(\\d+)"
                          :methods    [:get]
                          :match-type :regex
                          :handlers   [async-return-request-handler]})

(def multi-regex-path-variable {:path       "/admin/(\\d+)/([x-z]{1}-dept)"
                                :methods    [:get]
                                :match-type :regex
                                :handlers   [async-return-request-handler]})

(def blocking-handler {:path         "/blocking-handler"
                       :methods      [:get]
                       :handler-mode :blocking
                       :handlers     [(fn [_req]
                                        {:body "hit /blocking-handler"})]})

(def ^{:private true :const true}
  default-options {:port 16969 :event-loops 1})

(defn- launch-server [opts]
  (let [instance (donkey/create-server (merge default-options opts))]
    (server/startSync instance)
    instance))

(defn- ^WebClient launch-client [^Vertx vertx]
  (WebClient/create
    vertx
    (-> (WebClientOptions.)
        (.setDefaultHost "localhost")
        (.setDefaultPort (int (:port default-options))))))

(defn- get-vertx-instance [^DonkeyServer donkey-server]
  (.vertx ^Server (.-impl donkey-server)))

(def ^:dynamic donkey-server)
(def ^:dynamic ^WebClient client)

(defn- init
  "Initializes the server and client instances that are used throughout the tests"
  [test-fn]
  (binding [donkey-server (launch-server
                            {:routes [root-200
                                      ring-spec
                                      blocking-handler
                                      single-path-variable
                                      multi-path-variable
                                      regex-path-variable
                                      multi-regex-path-variable]})]
    (binding [client (launch-client (get-vertx-instance donkey-server))]
      (test-fn)
      (.close client)
      (is (nil? (server/stopSync donkey-server))))))

(use-fixtures :once init)

(defn- ^HttpResponse wait-for-response
  "Waits (blocks) until the `response-promise` is resolved.
  Returns the result on success, or throws the exception if failed."
  [response-promise]
  (let [^Future future-result @response-promise]
    (when (.failed future-result)
      (throw (.cause future-result)))
    (.result future-result)))

(defn- parse-response-body [^HttpResponse res]
  (-> res .bodyAsString clojure.edn/read-string))

(defn- parse-response-body-when-resolved [response-promise]
  (-> response-promise wait-for-response parse-response-body))

(defn- ^Handler create-client-handler
  "Create a handler that resolves `response-promise` when the client receives a response"
  [response-promise]
  (server/make-handler (fn [^Future res] (deliver response-promise res))))

(deftest test-basic-functionality
  (testing "the server should return a 200 response"
    (let [response-promise (promise)]
      (-> client
          ^HttpRequest (.get "/")
          (.send (create-client-handler response-promise)))

      (let [res (wait-for-response response-promise)]
        (is (= 200 (.statusCode res)))))))

(deftest test-ring-compliant-request
  (testing "it should include all the fields as specified by
  https://github.com/ring-clojure/ring/blob/master/SPEC"
    (let [response-promise (promise)]

      (-> client
          ^HttpRequest (.get (str "/ring-spec?foo=bar"))
          (.putHeader "DNT" "1")
          (.send (create-client-handler response-promise)))

      (let [^ILookup res (parse-response-body-when-resolved response-promise)]
        (is (= (:port default-options) (:server-port res)))
        (is (= (str "localhost:" (:port default-options)) (:server-name res)))
        (is (re-find #"127\.0\.0\.1:\d+" (:remote-addr res)))
        (is (= "/ring-spec" (:uri res)))
        (is (= "foo=bar" (:query-string res)))
        (is (= "bar" (-> res :query-params (get "foo"))))
        (is (= :http (:scheme res)))
        (is (= :get (:request-method res)))
        (is (= "HTTP/1.1" (:protocol res)))
        (is (= ["DNT" "host" "user-agent"] (keys (:headers res)))))))

  (testing "it should include the raw and parsed query parameters"
    (let [query-string "foo=bar&count=6&valid=true&empty=false&version=4.0.9&ratio=2.4"
          response-promise (promise)]

      (-> client
          ^HttpRequest (.get (str "/ring-spec?" query-string))
          (.send (create-client-handler response-promise)))

      (let [res (parse-response-body-when-resolved response-promise)]
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
        (-> client
            ^HttpRequest (.post "/user/12345")
            (.send (create-client-handler response-promise)))

        (let [res (parse-response-body-when-resolved response-promise)]
          (is (= {"id" "12345"} (:path-params res))))))

    (testing "Multiple path variables"
      (let [response-promise (promise)]
        (-> client
            ^HttpRequest (.put "/user/joe-shm0e123/marketing")
            (.send (create-client-handler response-promise)))

        (let [res (parse-response-body-when-resolved response-promise)]
          (is (= {"id" "joe-shm0e123" "department" "marketing"} (:path-params res))))))

    (testing "Single regex path variable with capturing group"
      (let [response-promise (promise)]
        (-> client
            ^HttpRequest (.get "/admin/909011")
            (.send (create-client-handler response-promise)))

        (let [res (parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "909011"} (:path-params res))))))

    (testing "Multiple regex path variables with capturing groups"
      (let [response-promise (promise)]
        (-> client
            ^HttpRequest (.get "/admin/10000/y-dept")
            (.send (create-client-handler response-promise)))

        (let [res (parse-response-body-when-resolved response-promise)]
          (is (= {"param0" "10000" "param1" "y-dept"} (:path-params res))))))

    (testing "when regex path doesn't match the route is not called"
      (let [response-promise (promise)]
        (-> client
            ^HttpRequest (.get "/admin/1-123-4")
            (.send (create-client-handler response-promise)))

        (let [res (wait-for-response response-promise)]
          (is (= 404 (.statusCode res)))))

      (let [response-promise (promise)]
        (-> client
            ^HttpRequest (.get "/admin/1234/xyz-dept")
            (.send (create-client-handler response-promise)))

        (let [res (wait-for-response response-promise)]
          (is (= 404 (.statusCode res))))))))

(deftest blocking-handler-test
  (testing "it should call the 1 argument arity handler"
    (let [response-promise (promise)]
      (-> client
          ^HttpRequest (.get "/blocking-handler")
          (.send (create-client-handler response-promise)))

      (let [res (wait-for-response response-promise)]
        (is (= 200 (.statusCode res)))
        (is (= "hit /blocking-handler" (.bodyAsString res)))))))
