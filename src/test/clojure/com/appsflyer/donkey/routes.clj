(ns com.appsflyer.donkey.routes)

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
   :handler      (fn [_req respond _raise] (respond {:status 200}))})

(def echo-route
  {:path         "/echo"
   :methods      [:get]
   :handler-mode :blocking
   :handler      return-request})

(def ring-spec
  {:path    "/ring-spec"
   :methods [:get]
   :handler async-return-request-handler})

(def single-path-variable
  {:path    "/user/:id"
   :methods [:post]
   :handler async-return-request-handler})

(def multi-path-variable
  {:path    "/user/:id/:department"
   :methods [:put]
   :handler async-return-request-handler})

(def regex-path-variable
  {:path       "/admin/(\\d+)"
   :methods    [:get]
   :match-type :regex
   :handler    async-return-request-handler})

(def multi-regex-path-variable
  {:path       "/admin/(\\d+)/([x-z]{1}-dept)"
   :methods    [:get]
   :match-type :regex
   :handler    async-return-request-handler})

(def blocking-handler
  {:path         "/blocking-handler"
   :methods      [:get]
   :handler-mode :blocking
   :handler      (fn [_req]
                   {:body "hit /blocking-handler"})})

(def explicit-consumes-json
  {:path         "/consumes/json"
   :methods      [:post]
   :consumes     ["application/json"]
   :handler-mode :blocking
   :handler      (fn [_req & _args] {})})

(def explicit-consumes-multi-part-or-form-encoded-or-octet-stream
  {:path         "/consumes/multi-urlencoded-stream"
   :methods      [:post]
   :consumes     ["multipart/form-data" "application/x-www-form-urlencoded" "application/octet-stream"]
   :handler-mode :blocking
   :handler      (fn [_req & _args] {})})

(defn- make-pre-processing-blocking-middleware [fun]
  (fn [handler]
    (fn [req]
      (handler (fun req)))))

(defn- make-post-processing-blocking-middleware [fun]
  (fn [handler]
    (fn [req]
      (fun (handler req)))))

(defn- make-pre-processing-middleware [fun]
  (fn [handler]
    (fn [req respond raise]
      (fun req (fn [res] (respond (handler res respond raise))) raise))))

(defn- make-post-processing-middleware [fun]
  (fn [handler]
    (fn [req respond raise]
      (handler req (fn [res] (respond (fun res respond raise))) raise))))

(def blocking-middleware-handlers
  {:path         "/route/middleware/blocking"
   :methods      [:get]
   :handler-mode :blocking
   :middleware   [(make-pre-processing-blocking-middleware
                    #(assoc % :counter 1))
                  (make-pre-processing-blocking-middleware
                    #(update % :counter inc))
                  (make-pre-processing-blocking-middleware
                    #(update % :counter inc))
                  (make-post-processing-blocking-middleware
                    (fn [res]
                      (update res :body #(str "{\"counter\":" (:counter %)
                                              ",\"success\":" (boolean (:success %)) "}"))))
                  (make-post-processing-blocking-middleware
                    #(update-in % [:body :success] str "e"))
                  (make-post-processing-blocking-middleware
                    #(update-in % [:body :success] str "u"))
                  (make-post-processing-blocking-middleware
                    #(update-in % [:body :success] str "r"))
                  (make-post-processing-blocking-middleware
                    #(update-in % [:body :success] str "t"))]
   :handler      (fn [req] {:status 200 :body {:counter (:counter req) :success ""}})})

(def async-middleware-handlers
  {:path         "/route/middleware/async"
   :methods      [:get]
   :handler-mode :non-blocking
   :middleware   [(make-pre-processing-middleware
                    (fn [req respond _raise] (respond (assoc req :counter 1))))
                  (make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (make-post-processing-middleware
                    (fn [res respond _raise]
                      (respond (update res :body #(str "{\"counter\":" (:counter %)
                                                       ",\"success\":" (boolean (:success %)) "}")))))
                  (make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "e"))))
                  (make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "u"))))
                  (make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "r"))))
                  (make-post-processing-middleware
                    (fn [res respond _raise] (respond (update-in res [:body :success] str "t"))))]
   :handler      (fn [req respond _raise] (respond {:status 200 :body {:counter (:counter req) :success ""}}))})


(def blocking-exceptional-middleware-handlers
  {:path         "/route/middleware/blocking/exception"
   :methods      [:get]
   :handler-mode :blocking
   :middleware   [(make-pre-processing-blocking-middleware #(assoc % :counter 1))
                  (make-pre-processing-blocking-middleware #(update % :counter inc))
                  (make-pre-processing-blocking-middleware #(update % :counter str))
                  (make-pre-processing-blocking-middleware #(update % :counter inc))
                  ; Should not be called
                  (make-pre-processing-blocking-middleware #(update % :counter inc))]
   ; Should not be called
   :handler      (fn [req]
                   {:status 200 :body (-> req :counter str .getBytes)})})

(def async-exceptional-middleware-handlers
  {:path         "/route/middleware/async/exception"
   :methods      [:get]
   :handler-mode :non-blocking
   :middleware   [(make-pre-processing-middleware
                    (fn [req respond _raise] (respond (assoc req :counter 1))))
                  (make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter inc))))
                  (make-pre-processing-middleware
                    (fn [req respond _raise] (respond (update req :counter str))))
                  (make-pre-processing-middleware
                    (fn [req respond raise]
                      (try
                        (respond (update req :counter inc))
                        (catch Exception ex
                          (raise ex)))))]
   ; Should not be called
   :handler      (fn [_req respond _raise] (respond {:status 200}))})
