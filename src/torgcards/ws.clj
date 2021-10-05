(ns torgcards.ws
  (:require [clojure.edn :as edn]))

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
    (case (:command message)
      :initialize (swap! db merge (:message message))
      :login (println (:message message) "is logging in"))))