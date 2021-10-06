(ns torgcards.ws
  (:require [clojure.edn :as edn]
            [org.httpkit.server :as kit]))

(defonce db (atom {}))

(defonce channels (atom #{}))

(defonce players (atom []))

(defn connect! [channel]
  (println "Channel opened")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! channels disj channel))

(defn temp-send-message! []
  (doseq [ch @channels]
    (kit/send! ch (pr-str @db))))

(defn add-player [ch player]
  (let [allowed-players (:player-count @db)]
    (cond
      (nil? allowed-players) (kit/send! ch (pr-str {}))
      (<= allowed-players (count @players)) (disconnect! ch "Too many players")
      :else (do
              (swap! players conj [ch player])
              (swap! db update-in [:players] conj player)
              (temp-send-message!)))))

(defn reset-db! []
  (reset! db {})
  (reset! channels #{})
  (reset! players [])
  (temp-send-message!))

(defn message! [channel ws-message]
  (let [message (edn/read-string ws-message)]
    (swap! db merge @db message)))



(comment

  (reset-db!)

  @players

  @channels
  (seq {:a 1})
  @db
  )