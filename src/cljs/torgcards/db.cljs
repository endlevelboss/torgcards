(ns torgcards.db
  (:require [re-frame.core :as rf]
            [torgcards.logic :as logic]))

(defonce channel (atom nil))

(rf/reg-sub
 :get-db
 (fn [db _]
   db))

(rf/reg-sub
 :player-name
 (fn [db _]
   (:player-name db)))

(rf/reg-event-db
 :set-player-name
 (fn [db [_ name]]
   (assoc db :player-name name)))

(defn send-message! [msg]
  (if-let [chan @channel]
    (.send chan (pr-str msg))
    (throw (js/Error. "Not connected"))))

(rf/reg-event-fx
 :update-db
 (fn [{:keys [db]} [_ vals]]
   {:db (merge db vals)}))

(rf/reg-event-fx
 :draw-drama
 (fn [{:keys [db]} [_ _]]
   (let [result (logic/draw-drama-card db)]
     (send-message! result)
     {:db (merge db result)})))

(rf/reg-event-fx
 :initialize-game
 (fn [_ [_ value]]
   (let [db (assoc logic/initial-db :player-count value)]
     (send-message! db)
     {:db db})))

(rf/reg-sub
 :current-drama
 (fn [db _]
   (:current-drama db)))


(defn handle-response! [response]
  (rf/dispatch [:update-db response]))