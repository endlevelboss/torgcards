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

(defn card-display [id path style options]
  (let [scale (if (nil? (:scale options)) 1 (:scale options))
        [width height] [292 410]
        [w h] (if (= :horizontal (:rotation options)) [(* scale height) (* scale width)] [(* scale width) (* scale height)])
        my-style (assoc style :width w :height h)
        myclass (if (nil? (:onclick options))
                  {:style my-style}
                  {:style my-style
                   :on-click #((:onclick options) id)})
        image (str path id ".jpg")]
    [:div myclass
     [:img {:src image
            :width w :height h
            :style {:position "relative"
                    :top 0 :left 0}}]]))

(defn player-login []
  (let [player (r/atom nil)]
    (fn []
      [:div
       [:label "Enter name: "]
       [:input {:type :text
                :value @player
                :on-change #(reset! player (-> % .-target .-value))
                :on-key-press #(if (= 13 (.-charCode %))
                                 (rf/dispatch [:set-player-name @player]))}]])))



(defn style-me [i scale offset horizontal?]
  (let [[top left] (if horizontal? [0 1] [1 0])
        [top-offset left-offset] (if horizontal? [offset 0] [0 offset])]
    {:position "absolute" :z-index i
     :top (+ top-offset (* top scale i)) :left (+ left-offset (* left scale i))}))



(defn other-players-view [player offset1 offset2 horizontal?]
  (let [{:keys [player-hand player-pool]} @(rf/subscribe [:player player])]
    [:div {:style {:position "absolute" :top 0 :left 0}}
     (for [[n i] (zipmap player-hand (range (count player-hand)))]
       ^{:key n} [card-display "back" "img/destiny/" (style-me i 40 offset1 horizontal?) {:scale 0.4}])
     (for [[n i] (zipmap player-pool (range (count player-pool)))]
       ^{:key n} [card-display n "img/destiny/" (style-me i 40 offset2 horizontal?) {:scale 0.4}])]))

(defn other-players-cosm [player offset1 offset2 horizontal?]
  (let [{:keys [cosm-hand cosm-pool cosm]} @(rf/subscribe [:player player])
        path (str "img/cosm/" cosm "/")]
    [:div {:style {:position "absolute" :top 0 :left 0}}
     (for [[n i] (zipmap cosm-hand (range (count cosm-hand)))]
       ^{:key n} [card-display "back" path (style-me 40 i offset1 horizontal?) {:scale 0.4}])
     (for [[n i] (zipmap cosm-pool (range (count cosm-pool)))]
       ^{:key n} [card-display n path (style-me i 40 offset2 horizontal?) {:scale 0.4}])]))

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

(defn give-card-button [name text function]
  (let [value (str text name)]
    [:input {:style {:width 160 :height 80}
             :type :button
             :value value
             :on-click #(rf/dispatch [function name])}]))

(defn nameplate [name top left]
  [:div {:style {:width 100 :height 25 :background-color "lightgray"
                 :position "absolute" :top top :left left
                 :border-style "solid" :border-radius 5
                 :border-width 2 :border-color "black"
                 :text-align "center"}}
   name])

(defn player-display [array index top left flip?]
  (if (>= index (count array))
    [:div]
    (let [[adj1 adj2] (if flip? [120 0] [0 120])
          name (nth array index)]
      [:div {:style {:position "absolute" :top top :left left}}
       [nameplate name 0 (+ adj1 5)]
       [:div {:style {:position "absolute" :top 208 :left 0}}
        [other-players-view name adj1 adj2 false]]
       [:div {:style {:position "absolute" :top 33 :left 0}}
        [other-players-cosm name adj1 adj2 false]]])))

(defn player-display-horizontal [array index top left]
  (if (>= index (count array))
    [:div]
    (let [name (nth array index)]
      [:div {:style {:position "absolute" :top top :left left}}
       [nameplate name 0 5]
       [:div {:style {:position "absolute" :top 35 :left 130}}
        [other-players-view name 0 168 true]]
       [:div {:style {:position "absolute" :top 35 :left 0}}
        [other-players-cosm name 0 168 true]]])))

(defn cosm-select [player]
  [:select {:name "cosm"
            :on-change #(rf/dispatch [:select-cosm {:player player :cosm (-> % .-target .-value)}])
            :style {:width 120 :height 30}}
   [:option {:value "nil"} "Select cosm:"]
   [:option {:value "aysle"} "Aysle"]
   [:option {:value "earth"} "Core Earth"]
   [:option {:value "cyberpapacy"} "Cyberpapacy"]
   [:option {:value "livingland"} "Living Land"]
   [:option {:value "nile"} "Nile Empire"]
   [:option {:value "orrorsh"} "Orrorsh"]
   [:option {:value "panpacifica"} "Pan-Pacifica"]
   [:option {:value "tharkold"} "Tharkold"]])

(defn gm-play []
  (let [current-drama @(rf/subscribe [:current-drama])
        players (seq @(rf/subscribe [:player-list]))]
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 40 :left 20}
            :src "img/torg/logo.png" :width 250}]
     [player-display players 0 187 0 false]
     [player-display-horizontal players 1 0 305 false true]
     [player-display players 2 187 850 true]
     [:div {:style {:position "absolute" :top 800 :left 300}}
      [card-display current-drama "img/drama/" nil {:rotation :horizontal}]
      [:div
       {:style {:position "absolute" :top 0 :left 430}
        :on-click #(rf/dispatch [:draw-drama nil])}
       [card-display "back" "img/drama/" nil {:rotation :horizontal :scale 0.2}]]
      [:input {:style {:position "absolute" :top 300 :left 0}
               :type :button
               :value "Reset"}]
      [:div {:style {:position "absolute" :top 70 :left 430}}
       (for [n players]
         ^{:key n} [give-card-button n "Destiny card: " :give-destiny-card])]
      [:div {:style {:position "absolute" :top 70 :left 595}}
       (for [n players]
         ^{:key n} [give-card-button n "Cosm card: " :give-cosm-card])]
      [:div {:style {:position "absolute" :top 70 :left 760}}
       (for [n players]
         ^{:key n} [cosm-select n])]]]))


(defn move-card [name from to]
  (fn [id]
    (rf/dispatch [:move-card-from-to {:name name :id id :from from :to to}])))

(defn player-play []
  (let [me @db/player-name
        {:keys [player-hand player-pool cosm-hand cosm-pool cosm]} @(rf/subscribe [:player me])
        players @(rf/subscribe [:player-list])
        other-players (seq (remove #{me} players))
        current-drama @(rf/subscribe [:current-drama])
        cosm-path (str "img/cosm/" cosm "/")]
    ;; (println "im playing " me)
    ;; (println player-hand)
    
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 0 :left 0}
            :src "img/torg/logo.png" :width 250}]
     [player-display (first other-players) 85 5 false]
     [player-display (last other-players) 85 890 true]
     [:div {:style {:position "absolute" :top 20 :left 360}}
      [card-display current-drama "img/drama/" nil {:rotation :horizontal :on-click nil}]]
     [:div {:style {:position "absolute" :top 900 :left 20}}
      (for [[n i] (zipmap cosm-hand (range (count cosm-hand)))]
        ^{:key n} [card-display n cosm-path (style-me i 220 0 true) {:scale 0.75
                                                                     :onclick (move-card me :cosm-hand :cosm-pool)}])]
     [:div {:style {:position "absolute" :top 590 :left 20}}
      (for [[n i] (zipmap cosm-pool (range (count cosm-pool)))]
        ^{:key n} [card-display n cosm-path (style-me i 220 0 true) {:scale 0.75
                                                                     :onclick (move-card me :cosm-pool :cosm-hand)}])]
     [:div {:style {:position "absolute" :top 900 :left 250}}
      (for [[n i] (zipmap player-hand (range (count player-hand)))]
        ^{:key n} [card-display n "img/destiny/" (style-me i 220 0 true) {:scale 0.75
                                                                          :onclick (move-card me :player-hand :player-pool)}])]
     [:div {:style {:position "absolute" :top 590 :left 250}}
      (for [[n i] (zipmap player-pool (range (count player-pool)))]
        ^{:key n} [card-display n "img/destiny/" (style-me i 220 0 true) {:scale 0.75
                                                                          :onclick (move-card me :player-pool :player-hand)}])]]))

(defn gm-view []
  (let [db? @(rf/subscribe [:get-db])]
    ;; (.log js/console (str "empty? " empty-db?))
    (if (seq db?)
      [gm-play]
      [gm-login])))

(defn player-view []
  (let [my-name @db/player-name]
    (if (nil? my-name)
      [player-login]
      [player-play])))

(defn mainview []
  (if (= "true" is-gm?)
    [:div
     [gm-view]
     [:div {:style {:position "absolute" :top 0 :left 1100}}
      [database-view]]]
    [:div 
     [player-view]
     [:div {:style {:position "absolute" :top 0 :left 1100}}
      [database-view]]])
  )
