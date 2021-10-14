(ns torgcards.db
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [torgcards.logic :as logic]))

(defonce channel (atom nil))

(def player-name (r/atom nil))

(rf/reg-sub
 :get-db
 (fn [db _]
   db))

(rf/reg-sub
 :player-name
 (fn [db _]
   (:player-name db)))



(rf/reg-event-db
 :update-db
 (fn [_ [_ vals]]
   vals))

(defn send-message! [msg]
  (if-let [chan @channel]
    (.send chan (pr-str msg))
    (throw (js/Error. "Not connected"))))

(rf/reg-fx
 :send
 (fn [value]
   (send-message! value)))

(rf/reg-fx
 :set-my-name
 (fn [value]
   (reset! player-name value)))


(rf/reg-event-fx
 :set-player-name
 (fn [_ [_ name]]
   {:send {:register-player name}
    :set-my-name name}))

(rf/reg-event-fx
 :draw-drama
 (fn [{:keys [db]} [_ _]]
   (let [result (logic/draw-drama-card db)]
     {:send result})))

(rf/reg-event-fx
 :initialize-game
 (fn [_ [_ value]]
   (let [db (assoc logic/initial-db :player-count value)]
     {:send db})))

(rf/reg-event-fx
 :give-destiny-card
 (fn [{:keys [db]} [_ name]]
   (let [result (logic/deal-destiny-card name db)]
     {:send result})))

(rf/reg-event-fx
 :select-cosm
 (fn [{:keys [db]} [_ {:keys [player cosm]}]]
   (let [result (logic/set-cosm-for-player player cosm db)]
     {:send result})))

(rf/reg-event-fx
 :give-cosm-card
 (fn [{:keys [db]} [_ name]]
   (let [result (logic/deal-cosm-card name db)]
     {:send result})))

(rf/reg-event-fx
 :move-card-from-to
 (fn [{:keys [db]} [_ {:keys [name id from to]}]]
   (let [result (logic/move-card db name id from to)]
     {:send result})))

(rf/reg-event-fx
 :return-card-from-pool
 (fn [{:keys [db]} [_ {:keys [name id]}]]
   (let [result (logic/return-from-pool db name id)]
     {:send result})))

(rf/reg-event-fx
 :discard-destiny
 (fn [{:keys [db]} [_ {:keys [player id]}]]
   (let [result (logic/discard-card id player db)]
     {:send result})))

(rf/reg-event-fx
 :discard-cosm
 (fn [{:keys [db]} [_ {:keys [player id]}]]
   (let [result (logic/discard-cosm id player db)]
     {:send result})))

(rf/reg-sub
 :current-drama
 (fn [db _]
   (:current-drama db)))

(rf/reg-sub
 :player
 (fn [db [_ name]]
   (get-in db [:players name])))

(rf/reg-sub
 :player-list
 (fn [db _]
   (:player-list db)))


(defn handle-response! [response]
  (.log js/console "got an update")
  (rf/dispatch [:update-db response]))