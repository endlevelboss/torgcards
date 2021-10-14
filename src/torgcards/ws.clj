(ns torgcards.ws
  (:require [clojure.edn :as edn]
            [org.httpkit.server :as kit]))

(defonce db (atom {}))

(defonce channels (atom #{}))

;; (defonce players (atom []))

(defn send-message! [msg]
  (println "Updating players!")
  (doseq [ch @channels]
    (kit/send! ch (pr-str msg))))

(defn connect! [channel]
  (println "Channel opened")
  (swap! channels conj channel)
  (send-message! @db))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! channels disj channel))



(defn add-player [ch player]
  (let [allowed-players (:player-count @db)
        player-list (:player-list @db #{})]
    (println "trying " allowed-players (count @channels))
    (cond
      ;; (some #{player} player-list) (disconnect! ch "Name in use")
      (nil? allowed-players) (disconnect! ch "Game not started")
      (< allowed-players (count @channels)) (disconnect! ch "Too many players")
      :else (do
              ;; (swap! players conj [ch player])
              (swap! db assoc-in [:player-list] (conj player-list player))
              (send-message! @db)))))

(some #{"jarl"} #{})

(defn reset-db! []
  (reset! db {})
  (reset! channels #{})
  ;; (reset! players [])
  (send-message! @db))

(defn message! [channel ws-message]
  (let [message (edn/read-string ws-message)]
    ;; (println message)
    (cond
      (contains? message :register-player) (add-player channel (:register-player message))
      (contains? message :register-gm) (println "registering gm")
      :else (do
              (reset! db (merge @db message))
              (send-message! @db)))))



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
    (send-message! msg)
    (swap! db merge @db msg))


  (seq (zipmap [1 2] [3 4]))
  )