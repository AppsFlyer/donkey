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

(def ^{:private true :const true}
  default-options {:port 16969 :event-loops 1})

(def root-200 {:path    "/"
               :methods [:get]
               :handler (fn [_req respond _raise] (respond {:status 200}))})

(def return-request {:path    "/ring-spec"
                     :methods [:get]
                     :handler (fn [req respond _raise]
                                (respond {:status 200
                                          :body   (.getBytes (pr-str (dissoc req :body)))}))})

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

(defn- init [test-fn]
  (binding [donkey-server (launch-server
                            {:routes [root-200
                                      return-request]})]
    (binding [client (launch-client (get-vertx-instance donkey-server))]
      (test-fn)
      (.close client)
      (is (nil? (server/stopSync donkey-server))))))

(use-fixtures :once init)

(defn- ^HttpResponse wait-for-response [response-promise]
  (let [^Future future-result @response-promise]
    (when (.failed future-result)
      (throw (.cause future-result)))
    (.result future-result)))

(defn- ^Handler get-client-handler [response-promise]
  (server/make-handler (fn [^Future res] (deliver response-promise res))))

(deftest test-basic-functionality
  (testing "the server should return a 200 response"
    (let [response-promise (promise)]
      (-> client
          ^HttpRequest (.get "/")
          (.send (get-client-handler response-promise)))

      (let [res (wait-for-response response-promise)]
        (is (= 200 (.statusCode res)))))))

(deftest test-ring-compliant-request
  (testing "it should include all the fields as specified by
  https://github.com/ring-clojure/ring/blob/master/SPEC"
    (let [response-promise (promise)]

      (-> client
          ^HttpRequest (.get (str "/ring-spec?foo=bar"))
          (.putHeader "DNT" "1")
          (.send (get-client-handler response-promise)))

      (let [^ILookup res (-> response-promise wait-for-response .bodyAsString clojure.edn/read-string)]
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
          (.send (get-client-handler response-promise)))

      (let [res (-> response-promise wait-for-response .bodyAsString clojure.edn/read-string)]
        (is (= query-string (:query-string res)))
        (is (= "bar" (-> res :query-params (get "foo"))))
        (is (= "6" (-> res :query-params (get "count"))))
        (is (= "true" (-> res :query-params (get "valid"))))
        (is (= "false" (-> res :query-params (get "empty"))))
        (is (= "4.0.9" (-> res :query-params (get "version"))))
        (is (= "2.4" (-> res :query-params (get "ratio"))))))))
