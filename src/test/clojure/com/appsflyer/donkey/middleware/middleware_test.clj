(ns com.appsflyer.donkey.middleware.middleware-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [com.appsflyer.donkey.util :as util]
            [com.appsflyer.donkey.middleware.params :refer [keywordize-query-params]]
            [com.appsflyer.donkey.routes :as routes])
  (:import (io.vertx.ext.web.client HttpRequest)
           (clojure.lang Symbol Keyword)))

(deftest test-keywordize-query-params
  (util/init
    (fn []
      (let [response-promise (promise)
            query-string "foo=bar&_count=6&:valid=true&:-empty=false&1=2&_"]
        (-> util/client
            ^HttpRequest (.get (str "/echo?" query-string))
            (.send (util/create-client-handler response-promise)))

        (let [res (util/parse-response-body-when-resolved response-promise)
              current-ns (str (the-ns *ns*))]

          (is (= query-string (:query-string res)))
          (is (= "bar" (-> res :query-params :foo)))
          (is (= "6" (-> res :query-params :_count)))
          (is (= "true" (get-in res [:query-params (Keyword/intern (Symbol/create current-ns "valid"))])))
          (is (= "false" (get-in res [:query-params (Keyword/intern (Symbol/create current-ns "-empty"))])))
          (is (= "2" (-> res :query-params :1)))
          (is (contains? (:query-params res) :_)))))
    [routes/echo-route]
    [keywordize-query-params]))
