(ns torgcards.ws
  (:require [clojure.edn :as edn]
            [torgcards.logic :as logic]
            [org.httpkit.server :as kit]))

(defonce db (atom {}))

(defonce channels (atom #{}))

(defonce usernames (atom {}))

(defonce users (atom {}))

(def players #{"jarl@jarl.ninja"
               "gustav.bilben@gmail.com"
               "mag-a@online.no"})

(def gm "tonyasle@gmail.com")

(defn send-to-player [ch msg]
  (let [player (get @users ch)
        username (if (= gm player) "gm" (get @usernames player))
        message (-> msg
                    (assoc :you {:id player :name username})
                    (assoc :player-list (seq @usernames)))]
    (kit/send! ch (pr-str message))))

(defn send-db! []
  (if (seq @db)
    @db
    (let [new-db (logic/initial-db)]
      (reset! db new-db)
      new-db)))

(defn send-message! []
  (let [msg (send-db!)]
    (doseq [ch @channels]
      (send-to-player ch msg))))

(defn add-player [email name]
  (if (= gm email)
    nil
    (swap! usernames assoc email name)))

(defn connect! [channel email name]
  (let [is-user? (some #{email} (conj players gm))]
    (if is-user?
      (do
        (println "Channel opened")
        (swap! channels conj channel)
        (swap! users assoc channel email)
        (add-player email name)
        (send-message!))
      (println "Refused connection for: " email))))

(defn disconnect! [channel status]
  (println "Channel closed " status)
  (swap! users dissoc channel)
  (swap! usernames dissoc channel)
  (swap! channels disj channel))

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
      :give-destiny-card (update-db value logic/deal-destiny-card)
      :select-cosm (update-db value logic/set-cosm-for-player)
      :give-cosm-card (update-db value logic/deal-cosm-card)
      :move-card-from-to (update-db value logic/move-card)
      :discard-destiny (update-db value logic/discard-card)
      :discard-cosm (update-db value logic/discard-cosm)
      :trade-card (update-db value logic/start-trade)
      :suggest-trade (update-db value logic/add-card-to-trade)
      :accept-trade (update-db value logic/accept-trade)
      :cancel-trade (update-db value logic/cancel-trade)
      :add-display-drama (update-db value logic/add-display-drama)
      :replace-drama (update-db value logic/replace-drama)
      :discard-drama (update-db value logic/discard-display)
      (println "unimplemented message " type))))