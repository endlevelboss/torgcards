(ns torgcards.gui
  (:require [re-frame.core :as rf]
            [reagent.core :as r]))

(def is-gm?
  (try 
    (-> (.getElementById js/document "gm")
        .-value)
    (catch js/Object e
      "false")))

(defn player-view []
  [:div "imma lowly player"])

(defn gm-login []
  (let [num (r/atom "")]
    (fn []
      [:div
       [:label "Input number of players"]
       [:input {:type :text
                :value @num
                :on-change #(reset! num (-> % .-target .-value))}]
       [:input {:type :button
                :value "Login"
                :on-click #(rf/dispatch [:initialize-game
                                         (int @num)])}]])))

(defn gm-view []
  (let [playercount @(rf/subscribe [:player-count])]
    (if (or (nil? playercount)
            (= 0 playercount))
      [gm-login]
      [:div
       [:div "im the Gaunt Man"]
       [:div "player count"]
       [:div playercount]])))



(defn mainview []
  (if (= "true" is-gm?)
    [gm-view]
    [player-view])
  )
