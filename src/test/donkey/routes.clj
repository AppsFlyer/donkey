(ns donkey.routes
  (:import (java.nio.charset StandardCharsets)))

(defn return-request [req]
  {:status 200
   :body   (-> (dissoc req :body)
               pr-str
               .getBytes)})

(defn async-return-request-handler
  "An asynchronous handler that returns the request in the response body"
  [req respond _raise]
  (-> req return-request respond))

(def root-200
  {:path         "/"
   :methods      [:get]
   :handler-mode :non-blocking
   :handlers     [(fn [_req respond _raise] (respond {:status 200}))]})

(def ring-spec
  {:path     "/ring-spec"
   :methods  [:get]
   :handlers [async-return-request-handler]})

(def single-path-variable
  {:path     "/user/:id"
   :methods  [:post]
   :handlers [async-return-request-handler]})

(def multi-path-variable
  {:path     "/user/:id/:department"
   :methods  [:put]
   :handlers [async-return-request-handler]})

(def regex-path-variable
  {:path       "/admin/(\\d+)"
   :methods    [:get]
   :match-type :regex
   :handlers   [async-return-request-handler]})

(def multi-regex-path-variable
  {:path       "/admin/(\\d+)/([x-z]{1}-dept)"
   :methods    [:get]
   :match-type :regex
   :handlers   [async-return-request-handler]})

(def blocking-handler
  {:path         "/blocking-handler"
   :methods      [:get]
   :handler-mode :blocking
   :handlers     [(fn [_req]
                    {:body "hit /blocking-handler"})]})

(def blocking-middleware-handlers
  {:path         "/route/middleware/blocking"
   :methods      [:get]
   :handler-mode :blocking
   :handlers     [(fn [req] (assoc req :counter 1))
                  (fn [req] (update req :counter inc))
                  (fn [req] (update req :counter inc))
                  (fn [req] {:status 200 :body {:counter (:counter req) :success ""}})
                  (fn [res] (update-in res [:body :success] str "t"))
                  (fn [res] (update-in res [:body :success] str "r"))
                  (fn [res] (update-in res [:body :success] str "u"))
                  (fn [res] (update-in res [:body :success] str "e"))
                  (fn [res] (update res :body #(.getBytes
                                                 (str "{\"counter\":" (:counter %)
                                                      ",\"success\":" (boolean (:success %)) "}")
                                                 StandardCharsets/UTF_8)))]})

(def async-middleware-handlers
  {:path         "/route/middleware/async"
   :methods      [:get]
   :handler-mode :non-blocking
   :handlers     [(fn [req respond _raise] (future (respond (assoc req :counter 1))))
                  (fn [req respond _raise] (future (respond (update req :counter inc))))
                  (fn [req respond _raise] (future (respond (update req :counter inc))))
                  (fn [req respond _raise] (future (respond {:status 200 :body {:counter (:counter req) :success ""}})))
                  (fn [res respond _raise] (future (respond (update-in res [:body :success] str "t"))))
                  (fn [res respond _raise] (future (respond (update-in res [:body :success] str "r"))))
                  (fn [res respond _raise] (future (respond (update-in res [:body :success] str "u"))))
                  (fn [res respond _raise] (future (respond (update-in res [:body :success] str "e"))))
                  (fn [res respond _raise] (future (respond (update res :body #(.getBytes
                                                                                 (str "{\"counter\":" (:counter %)
                                                                                      ",\"success\":" (boolean (:success %)) "}")
                                                                                 StandardCharsets/UTF_8)))))]})

(def blocking-exceptional-middleware-handlers
  {:path         "/route/middleware/blocking/exception"
   :methods      [:get]
   :handler-mode :blocking
   :handlers     [(fn [req] (assoc req :counter 1))
                  (fn [req] (update req :counter inc))
                  (fn [req] (update req :counter str))
                  (fn [req] (update req :counter inc))
                  ; Should not be called
                  (fn [_req] {:status 200})]})

(def async-exceptional-middleware-handlers
  {:path         "/route/middleware/async/exception"
   :methods      [:get]
   :handler-mode :non-blocking
   :handlers     [(fn [req respond _raise] (future (respond (assoc req :counter 1))))
                  (fn [req respond _raise] (future (respond (update req :counter inc))))
                  (fn [req respond _raise] (future (respond (update req :counter str))))
                  (fn [req respond raise] (future
                                            (try
                                              (respond (update req :counter inc))
                                              (catch Exception ex
                                                (raise ex)))))
                  ; Should not be called
                  (fn [_req respond _raise] (future (respond {:status 200})))]})

(def blocking-global-middleware
  {:path         "/middleware/blocking"
   :methods      [:get]
   :handler-mode :blocking
   :handlers     [return-request]})

