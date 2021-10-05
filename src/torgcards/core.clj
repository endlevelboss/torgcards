(ns torgcards.core
  (:require [org.httpkit.server :as kit]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as reitit]
            [muuntaja.middleware :as muuntaja]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [hiccup.page :as page])
  (:gen-class))

(defonce db (atom {}))

(defonce channels (atom #{}))

(defonce players (atom {}))

(defn connect! [channel]
  (println "Channel opened")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! channels disj channel))

(defn message! [channel ws-message]
  (let [message (edn/read-string ws-message)]
    (swap! db merge message)))

(defn html-handler [request-map]
  ;; (pp/pprint request-map)
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Lodading script"]
                [:img {:src "img/destiny/5.jpg"}]]
               (page/include-js "/js/app.js"))))

(defn gm-handler [request-map]
  ;; (pp/pprint request-map)
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Loading script"]
                [:input {:id "gm" :type "hidden" :value "true"}]]
               (page/include-js "/js/app.js"))))

(defn ws-handler [req]
  (kit/as-channel req
                  {:on-receive (fn [ch message] (message! ch message))
                   :on-open (fn [ch] (connect! ch))
                   :on-close (fn [ch status] (disconnect! ch status))}))

(def routes
  [["/" {:get html-handler}]
   ["/asle" {:get gm-handler}]
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

  (doseq [channel @channels]
    (kit/send! channel (pr-str {:player-count 1})))
  (pr-str {:a 1})
  @channels

  @db

  )