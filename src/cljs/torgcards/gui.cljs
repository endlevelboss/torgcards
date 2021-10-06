(ns torgcards.gui
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [torgcards.db :as db]))

(def is-gm?
  (try 
    (-> (.getElementById js/document "gm")
        .-value)
    (catch js/Object e
      "false")))

(defn on-key-press [event value command]
  (if (= 13 (.-charCode event))
    (db/send-message! value)))

;; (defn login-enter [event value]
;;   (on-key-press event value :login)
;;   (rf/dispatch [:set-player-name value]))

(defn player-login []
  (let [player (r/atom nil)]
    (fn []
      [:div
       [:label "Enter name: "]
       [:input {:type :text
                :value @player
                :on-change #(reset! player (-> % .-target .-value))
                :on-key-press #(if (= 13 (.-charCode %))
                                 (do
                                   (db/send-message! @player)
                                   (rf/dispatch [:set-player-name @player])))}]])))

(defn player-view []
  (let [my-name @(rf/subscribe [:player-name])]
    (if (nil? my-name)
      [player-login]
      [:div my-name])))

(defn gm-login []
  (let [num (r/atom "")]
    (fn []
      [:div
       [:label "Input number of players"]
       [:input {:type :text
                :value @num
                :on-change #(reset! num (-> % .-target .-value))
                :on-key-press #(if (= 13 (.-charCode %))
                                 (rf/dispatch [:initialize-game (int @num)]))}]])))

(defn database-view []
  (let [mydb @(rf/subscribe [:get-db])]
    [:div
     (for [[k v] (seq mydb)]
       ^{:key k}[:div (str k " : " v)])]))

(defn gm-play []
  (let [current-drama @(rf/subscribe [:current-drama])]
    [:div "torg game started"
     [:div 
      {:style {:width 50 :height 50 :background-color "green"}}
      current-drama]
     [:div
      {:style {:width 50 :height 50 :background-color "red"}
       :on-click #(rf/dispatch [:draw-drama nil])}
      "drama-deck"]
     [database-view]]))

(defn gm-view []
  (let [db? @(rf/subscribe [:get-db])]
    ;; (.log js/console (str "empty? " empty-db?))
    (if (seq db?)
      [gm-play]
      [gm-login])))



(defn mainview []
  (if (= "true" is-gm?)
    [gm-view]
    [player-view])
  )
