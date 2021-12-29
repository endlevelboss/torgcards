(ns torgcards.db
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [torgcards.logic :as logic]))

(defonce channel (atom nil))

;; (defonce window-height (atom 0))

;; (defn set-window-height! [height]
;;   (reset! window-height height))

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
 :set-window-height
 (fn [db [_ hth]]
   (assoc db :window-height hth)))

(rf/reg-sub
 :window-height
 (fn [db _]
   (:window-height db)))



(rf/reg-event-db
 :update-db
 (fn [_ [_ vals]]
   (let [hth (. js/window -innerHeight)]
     (assoc vals :window-height hth))))

(defn send-message! [msg]
  (if-let [chan @channel]
    (.send chan (pr-str msg))
    (throw (js/Error. "Not connected"))))

(rf/reg-fx
 :send
 (fn [msg]
   (send-message! msg)))

(rf/reg-fx
 :set-my-name
 (fn [value]
   (reset! player-name value)))

(rf/reg-sub
 :display-drama
 (fn [db _]
   (:display-drama db)))

(rf/reg-event-fx
 :add-display-drama
 (fn [_ _]
   {:send {:type :add-display-drama :value nil}}))

(rf/reg-event-fx
 :replace-drama
 (fn [_ [_ card]]
   {:send {:type :replace-drama :value card}}))

(rf/reg-event-fx
 :discard-drama
 (fn [_ [_ card]]
   {:send {:type :discard-drama :value card}}))

(rf/reg-event-fx
 :set-player-name
 (fn [_ [_ name]]
   {:send {:type :register-player :value name}
    :set-my-name name}))

(rf/reg-event-fx
 :draw-drama
 (fn [_ _]
   {:send {:type :draw-drama :value "hi there"}}))

(rf/reg-event-fx
 :initialize-game
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :give-destiny-card
 (fn [_ [type name]]
   {:send {:type type :value name}}))

(rf/reg-event-fx
 :select-cosm
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :give-cosm-card
 (fn [_ [type name]]
   {:send {:type type :value name}}))

(rf/reg-event-fx
 :move-card-from-to
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :trade-card
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :suggest-trade
 (fn [_ [type value]]
   {:send {:type type :value value}}))

;; (rf/reg-event-fx
;;  :return-card-from-pool
;;  (fn [{:keys [db]} [_ {:keys [name id]}]]
;;    (let [result (logic/return-from-pool db name id)]
;;      {:send result})))

(rf/reg-event-fx
 :discard-destiny
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :discard-cosm
 (fn [_ [type value]]
   {:send {:type type :value value}}))

(rf/reg-event-fx
 :accept-trade
 (fn [_ [type _]]
   {:send {:type type :value nil}}))

(rf/reg-event-fx
 :cancel-trade
 (fn [_ [type _]]
   {:send {:type type :value nil}}))

(rf/reg-sub
 :current-drama
 (fn [db _]
   (:current-drama db)))

(rf/reg-sub
 :player
 (fn [db [_ name]]
   (get-in db [:players name])))

(rf/reg-sub
 :me
 (fn [db _]
   (get-in db [:you])))

(rf/reg-sub
 :player-list
 (fn [db _]
   (:player-list db)))

(rf/reg-sub
 :trade
 (fn [db _]
   (:trade db)))


(defn handle-response! [response]
  (.log js/console "got an update")
  (rf/dispatch [:update-db response]))