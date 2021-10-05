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
    (db/send-message! command nil value)))

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
                                   (db/send-message! :login @player @player)
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
                                 (rf/dispatch [:initialize-game @num]))}]])))

(defn gm-view []
  (let [playercount @(rf/subscribe [:player-count])]
    (if (or (nil? playercount)
            (= 0 playercount))
      [gm-login]
      [:div
       [:div "player count"]
       [:div playercount]])))



(defn mainview []
  (if (= "true" is-gm?)
    [gm-view]
    [player-view])
  )
