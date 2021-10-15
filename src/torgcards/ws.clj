(ns torgcards.ws
  (:require [clojure.edn :as edn]
            [torgcards.logic :as logic]
            [org.httpkit.server :as kit]))

(defonce db (atom {}))

(defonce channels (atom #{}))

;; (defonce players (atom []))

(defn send-message! []
  (let [msg @db]
    (doseq [ch @channels]
      (kit/send! ch (pr-str msg)))))

(defn connect! [channel]
  (println "Channel opened")
  (swap! channels conj channel)
  (send-message!))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! channels disj channel))



(defn add-player [ch player]
  (let [allowed-players (:player-count @db)
        player-list (:player-list @db #{})]
    (cond
      (nil? allowed-players) (disconnect! ch "Game not started")
      (< allowed-players (count @channels)) (disconnect! ch "Too many players")
      :else (do
              (swap! db assoc-in [:player-list] (conj player-list player))
              (send-message!)))))

(defn reset-db! []
  (reset! db {})
  (reset! channels #{})
  ;; (reset! players [])
  (send-message!))

(defn old-message! [channel ws-message]
  (let [message (edn/read-string ws-message)]
    ;; (println message)
    (cond
      (contains? message :register-player) (add-player channel (:register-player message))
      (contains? message :register-gm) (println "registering gm")
      :else (do
              (reset! db (merge @db message))
              (send-message!)))))

(defn initialize-game [num-of-players]
  (let [new-db (assoc (logic/initial-db) :player-count num-of-players)]
    (reset! db new-db)
    (send-message!)))

(defn update-db [value f]
  (swap! db f value)
  (send-message!))

(defn message! [channel ws-message]
  (let [{:keys [type value]} (edn/read-string ws-message)]
    (case type
      :initialize-game (initialize-game value)
      :draw-drama (update-db value logic/draw-drama-card)
      :register-player (add-player channel value)
      :give-destiny-card (update-db value logic/deal-destiny-card)
      :select-cosm (update-db value logic/set-cosm-for-player)
      :give-cosm-card (update-db value logic/deal-cosm-card)
      :move-card-from-to (update-db value logic/move-card)
      :discard-destiny (update-db value logic/discard-card)
      :discard-cosm (update-db value logic/discard-cosm)
      (println "unimplemented message " type))))


(comment

  (merge {:a {:b {:c 1 :d 2}}} {:a {:b {:d 3}}})

  (into [] (remove #{3} [1 2 3 4]))

  (reset-db!)

  ;; @players

  @channels
  (seq {:a 1})
  @db

  (remove #{3} #{1 2 3})

  (let [msg {:player-list #{"jarl" "gustav" "magnus"}
             :players {"jarl" {:player-hand [5 7 56]
                               :player-pool [3 8]
                               :cosm-hand [5]
                               :cosm-pool []
                               :cosm "nile"}
                       "gustav" {:player-hand [11 12 14]
                                 :player-pool [15]
                                 :cosm-hand [7]
                                 :cosm-pool []
                                 :cosm "tharkold"}
                       "magnus" {:player-hand [16 17]
                                 :player-pool []
                                 :cosm-hand []
                                 :cosm-pool [1]
                                 :cosm "panpacifica"}}}]
    (send-message!)
    (swap! db merge @db msg))

  (swap! db assoc :player-list #{"jarl"})
  (send-message!)

  (seq (zipmap [1 2] [3 4]))
  )