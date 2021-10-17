(ns torgcards.core
  (:require [org.httpkit.server :as kit]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as reitit]
            [muuntaja.middleware :as muuntaja]
            [clojure.pprint :as pp]
            [hiccup.page :as page]
            [torgcards.ws :as ws]))

(defn html-handler [request-map]
  ;; (pp/pprint request-map)
  (response/ok
   (page/html5 [:body
                [:div {:id "content"} "Lodading script"]]
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
                  {:on-receive (fn [ch message] (ws/message! ch message))
                   :on-open (fn [ch] (ws/connect! ch))
                   :on-close (fn [ch status] (ws/disconnect! ch status))}))

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




(defn start [port]
  (kit/run-server
   (-> #'handler
       wrap-reload)
   {:port port
    :join? false})
  (println "server started"))

;; (defn stop []
;;   (kit/server-stop! @server))

(comment
  (start 3000)

  (map #(hash-map :a :b :c %) [1 2 3])

  )