(ns torgcards.core
  (:require [org.httpkit.server :as kit]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as reitit]
            [muuntaja.middleware :as muuntaja]
            [clojure.pprint :as pp]
            [hiccup.page :as page])
  (:gen-class))

(defonce channels (atom #{}))

(defn connect! [channel]
  (println "Channel opened")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! channels disj channel))

(defn html-handler [request-map]
  ;; (pp/pprint request-map)
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Here comes hopefully content"]
                [:img {:src "img/destiny/5.jpg"}]]
               (page/include-js "/js/app.js"))))

(defn ws-handler [req]
  (kit/as-channel req
                  {:on-receive (fn [ch message] (println "got message"))
                   :on-open (fn [ch] (connect! ch))
                   :on-close (fn [ch status] (disconnect! ch status))}))

(def routes
  [["/" {:get html-handler}]
   ["/ws" {:get ws-handler}]])


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

;; (defn stop []
;;   (kit/server-stop! @server))

(comment
  (start)


  @channels

  )