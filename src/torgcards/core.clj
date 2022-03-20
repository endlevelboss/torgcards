(ns torgcards.core
  (:require [org.httpkit.server :as kit]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as reitit]
            [hiccup.page :as page]
            [torgcards.ws :as ws])
  (:gen-class))

(defn html-handler [request-map]
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Lodading script"]]
               (page/include-js "/js/app.js"))))

(defn gm-handler [request-map]
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Loading script"]
                [:input {:id "gm" :type "hidden" :value "true"}]]
               (page/include-js "/js/app.js"))))

(defn ws-handler [{:keys [path-params] :as req}]
  (let [email (:email path-params)
        user email]
    (kit/as-channel req
                    {:on-receive (fn [ch message] (ws/message! ch message))
                     :on-open (fn [ch] (ws/connect! ch email user))
                     :on-close (fn [ch status] (ws/disconnect! ch status))})))
(def routes
  [["/" {:get html-handler}]
   ["/asle" {:get gm-handler}]
   ["/user/:email" {:get ws-handler}]])

(def handler
  (reitit/ring-handler
   (reitit/router routes)
   (reitit/routes
    (reitit/create-resource-handler {:path "/"})
    (reitit/create-default-handler
     {:not-found (constantly (response/not-found "404 Not found"))
      :method-not-allowed (constantly (response/method-not-allowed "405 Not allowed"))
      :not-acceptable (response/not-acceptable "406 Not acceptable")}))))

(defn -main []
  (println "Hello World"))

(defn start []
  (kit/run-server
   (-> #'handler
       wrap-reload)
   {:port 3000
    :join? false})
  (println "server started"))


(defn fizzbuzz [n] 
  (let [fizzes (cycle ["" "" "Fizz"]) 
        buzzes (cycle ["" "" "" "" "Buzz"]) 
        words (map str fizzes buzzes) 
        numbers (map str (rest (range)))] 
    (take n (map max words numbers))))



(comment

  (start)

  )