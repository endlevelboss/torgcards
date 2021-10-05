(ns torgcards.db
  (:require [re-frame.core :as rf]
            [torgcards.logic :as logic]))

(defonce channel (atom nil))

(rf/reg-event-db
 :update-db
 (fn [db [_ new-db]]
   new-db))

(defn handle-response! [response]
  (rf/dispatch [:update-db response]))

(rf/reg-sub
 :player-count
 (fn [db _]
   (:player-count db)))

(defn send-message! [msg]
  (if-let [chan @channel]
    (.send chan (pr-str msg))
    (throw (js/Error. "Not connected"))))

(rf/reg-event-fx
 :initialize-game
 (fn [_ [_ value]]
   (let [db (assoc logic/initial-db :player-count value)]
     (send-message! db)
     {:db db})))