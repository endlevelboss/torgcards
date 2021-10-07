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


;; (defn login-enter [event value]
;;   (on-key-press event value :login)
;;   (rf/dispatch [:set-player-name value]))

(defn card-display [id style]
  (let [my-style (assoc style :width 73 :sheight 102)]
    [:div {:style my-style}
     [:img {:src "img/drama/back.jpg"
            :width 73 :height 102
            :style {:position "relative"
                    :top 0 :left 0}}]
     [:div {:style {:position "relative"
                    :top -20 :left 20
                    :width 20 :height 20 :background-color "white"}}
      id]]))

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

(defn style-me [i offset horizontal?]
  (let [[top left] (if horizontal? [0 1] [1 0])
        [top-offset left-offset] (if horizontal? [offset 0] [0 offset])]
    {:position "absolute"
     :top (+ top-offset (* top 40 i)) :left (+ left-offset (* left 40 i))}))



(defn other-players-view [player offset1 offset2 horizontal?]
  (let [{:keys [player-hand player-pool]} @(rf/subscribe [:player player])]
    ;; (.log js/console (str "listening to " player))
    ;; (.log js/console (str player-hand player player-pool))
    [:div {:style {:position "absolute" :top 0 :left 0}}
     (for [[n i] (zipmap player-hand (range (count player-hand)))]
       ^{:key n} [card-display n (style-me i offset1 horizontal?)])
     (for [[n i] (zipmap player-pool (range (count player-pool)))]
       ^{:key n} [card-display n (style-me i offset2 horizontal?)])]))

(defn database-view []
  (let [mydb @(rf/subscribe [:get-db])]
    [:div
     (for [[k v] (seq mydb)]
       ^{:key k} [:div (str k " : " v)])]))

(defn gm-login []
  (let [num (r/atom "")]
    (fn []
      [:div
       [:label "Input number of players"]
       [:input {:type :text
                :value @num
                :on-change #(reset! num (-> % .-target .-value))
                :on-key-press #(if (= 13 (.-charCode %))
                                 (rf/dispatch [:initialize-game (int @num)]))}]
       [database-view]])))

(defn give-card-button [name]
  (let [value (str "Give " name " a card")]
    [:input {:type :button
             :value value
             :on-click #(rf/dispatch [:give-destiny-card name])}]))

(defn gm-play []
  (let [current-drama @(rf/subscribe [:current-drama])]
    (.log js/console "am i updating?")
    [:div {:style {:position "absolute"}}
     "its aardwark and started"
     [:div {:style {:position "absolute" :top 210 :left 0}}
      [other-players-view "gustav" 0 75 false]]
     [:div {:style {:position "absolute" :top 0 :left 200}}
      [other-players-view "jarl" 0 105 true]]
     [:div {:style {:position "absolute" :top 210 :left 500}}
      [other-players-view "magnus" 75 0 false]]
     [:div {:style {:position "absolute" :top 500 :left 200}}
      [:div {:style {:position "absolute" :top 0 :left 0}}
       [card-display current-drama]]
      [:div
       {:style {:position "absolute" :top 0 :left 110}
        :on-click #(rf/dispatch [:draw-drama nil])}
       [card-display nil]]
      [:div {:style {:position "absolute" :top 0 :left 200}}
       [:input {:type :button
                :value "Reset"}]
       (for [n ["gustav" "jarl" "magnus"]]
         ^{:key n}[give-card-button n])]]
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
