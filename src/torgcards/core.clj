(ns torgcards.core
  (:require [org.httpkit.server :as kit]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.ring :as reitit]
            [hiccup.page :as page]
            [torgcards.ws :as ws]
            [clojure.edn :as edn])
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

(defn remove-card [pool card]
  (if (nil? card)
    pool
    (disj pool card)))

(defn add-card [pool card]
  (if (nil? card)
    pool
    (conj (into [] pool) card)))

(defn remove-and-add [pool outcard incard]
  (let [p (into #{} pool)]
    (-> (remove-card p outcard)
        (add-card incard))))

(defn trade [player1 player2 card1 card2]
  (let [db @ws/db
        p1 (get-in db [:players player1 :player-pool])
        p2 (get-in db [:players player2 :player-pool])
        new1 (remove-and-add p1 card1 card2)
        new2 (remove-and-add p2 card2 card1)]
    (reset! ws/db
            (-> (assoc-in db [:players player1 :player-pool] new1)
                (assoc-in [:players player2 :player-pool] new2)))
    (ws/send-message!)))



(defn gus->jarl [card1 card2]
  (trade "gustav.bilben@gmail.com" "jarl@jarl.ninja" card1 card2))

(defn gus->mag [card1 card2]
  (trade "gustav.bilben@gmail.com" "mag-a@online.no" card1 card2))

(defn jarl->mag [card1 card2]
  (trade "jarl@jarl.ninja" "mag-a@online.no" card1 card2))


(defn save-db []
  (spit "card-state.edn" @ws/db))

(defn load-db []
  (let [f (-> (slurp "card-state.edn")
              edn/read-string)]
    (reset! ws/db f)
    (ws/send-message!)))



(comment
  (load-db)
  
  (slurp "card-state.edn")

  (gus->jarl 11 nil)
  (jarl->mag 22 13)

  (start)

  (-> @ws/db
      clojure.pprint/pprint)

  (->> (assoc @ws/db :names ws/player-names)
       (reset! ws/db))

  (->> (assoc-in @ws/db [:players "jarl@jarl.ninja" :cosm-pool] [])
       (reset! ws/db))

  (ws/send-message!)

  (reset! ws/db {})
  )