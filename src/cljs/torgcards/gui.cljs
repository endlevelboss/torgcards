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
  (if (nil? id)
    [:div]
    (let [scale (if (nil? (:scale options)) 1 (:scale options))
          [width height] [292 410]
          [w h] (if (= :horizontal (:rotation options)) [(* scale height) (* scale width)] [(* scale width) (* scale height)])
          my-style (assoc style :width w :height h
                          :border-radius 5 :overflow "hidden")
          myclass (if (nil? (:onclick options))
                    {:style my-style}
                    {:style my-style
                     :on-click #((:onclick options) id)})
          image (str path id ".jpg")]
      [:div myclass
       [:img {:src image
              :width w :height h
              :style {:position "absolute"
                      :top 0 :left 0}}]])))

(defn player-login []
  (let [player (r/atom nil)]
    (fn []
      [:div {:style {:position "absolute" :top 400 :left 300
                     :width 300 }}
       [:img {:src "img/torg/logo.png" :width 250}]
       [:div {:style {:position "relative" :left 50}} "Enter character name: "]
       [:input {:style {:position "relative" :left 30}
                :type :text
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

(defn trade-window []
  (let [{:keys [player1 player2 card1 card2] :as trade} @(rf/subscribe [:trade])
        ready-to-trade? (if (nil? card2) true false)
        me @db/player-name]
    (if (seq trade)
      [:div {:style {:position "absolute" :top 0 :left 0 :width 535 :height 320 :background-color "lightblue"
                     :border-style "solid" :border-color "black" :border-radius 5 :border-width 3
                     :overflow "hidden"}}
       [:input {:type :button :value "OK!"
                :style {:position "absolute" :top 10 :left 5
                        :width 80 :height 40}
                :disabled ready-to-trade?
                :on-click #(if (= me player1)
                             (rf/dispatch [:accept-trade])
                             nil)}]
       [:input {:type :button :value "Cancel"
                :style {:position "absolute" :top 53 :left 5
                        :width 80 :height 40}
                :on-click #(if (= me player1)
                             (rf/dispatch [:cancel-trade])
                             nil)}]
       [card-display card1 "img/destiny/" {:position "absolute" :top 5 :left 90} {:scale 0.75}]
       [card-display card2 "img/destiny/" {:position "absolute" :top 5 :left 310} {:scale 0.75}]]
      [:div])))

(defn gm-play []
  (let [current-drama @(rf/subscribe [:current-drama])
        players (seq @(rf/subscribe [:player-list]))]
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 40 :left 20}
            :src "img/torg/logo.png" :width 250}]
     [player-display players 0 187 0 false]
     [player-display-horizontal players 1 0 305 false true]
     [player-display players 2 187 850 true]
     [:div {:style {:position "absolute" :top 800 :left 200}}
      [card-display "back" "img/drama/" nil {:rotation :horizontal}]
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
         ^{:key n} [cosm-select n])]
      ]
     [:div {:style {:position "absolute" :top 400 :left 275}}
      [trade-window]]]))


(defn move-card [name from to]
  (fn [id]
    (rf/dispatch [:move-card-from-to {:name name :id id :from from :to to}])))

(defn tag-cards [cards type]
  (map #(hash-map :value % :type type) cards))

(defn combine-lists [destinycards cosmcards]
  (let [d (tag-cards destinycards :destiny)
        c (tag-cards cosmcards :cosm)]
    (into c d)))

(defn trade-card []
  (fn [id]
    (rf/dispatch [:suggest-trade id])))

(defn display-all-cards [{:keys [value type]} hand? cosm player style]
  (let [trade @(rf/subscribe [:trade])
        me @db/player-name
        path (if (= :cosm type) (str "img/cosm/" cosm "/") "img/destiny/")
        [hand pool] (if (= :cosm type) [:cosm-hand :cosm-pool] [:player-hand :player-pool])
        [to from] (if hand? [pool hand] [hand pool])
        clickfunction (if (seq trade)
                        (cond
                          (= me (:player1 trade)) nil
                          (and (= me (:player2 trade))
                               (not hand?)) (trade-card)
                          (and (= me (:player2 trade))
                               hand?) nil
                          :else (move-card player from to))
                        (move-card player from to))]
    [card-display value path style {:scale 0.75 :onclick clickfunction}]))

(defn discard-button [{:keys [value type]} name style]
  (let [s (assoc style :width 25 :height 25 :background-color "red"
                 :border-style "solid" :border-radius 15 :border-width 2 :border-color "black"
                 :text-align "center" :margin "auto" :color "white" :user-select "none")
        {:keys [player1 player2]} @(rf/subscribe [:trade])
        me @db/player-name]
    (if (or (= me player1) (= me player2))
      [:div]
      [:div {:style s
             :on-click #(if (= type :cosm)
                          (rf/dispatch [:discard-cosm {:player name :id value}])
                          (rf/dispatch [:discard-destiny {:player name :id value}]))}
       "X"])))

(defn trade-button [{:keys [value type]} me player style indx]
  (if (= :cosm type)
    [:div]
    (let [top (* indx 30)
          s (assoc style :width 25 :height 25 :background-color "darkgreen"
                   :border-style "solid" :border-radius 15 :border-width 2 :border-color "black"
                   :text-align "center" :margin "auto" :color "white" :user-select "none" :overflow "hidden"
                   :top top)]
      [:div {:style s
             :on-click #(rf/dispatch [:trade-card {:player1 me :player2 player :card1 value}])}
       player])))

(defn trade-buttons [player me indx pools]
  (let [trade? (seq @(rf/subscribe [:trade]))]
    (if trade?
      [:div]
      [:div
       (for [[n i] (zipmap pools (range (count pools)))]
         ^{:key n} [trade-button n me player (style-me i 220 0 true) indx])])))



(defn player-play []
  (let [me @db/player-name
        {:keys [player-hand player-pool cosm-hand cosm-pool cosm]} @(rf/subscribe [:player me])
        players @(rf/subscribe [:player-list])
        other-players (seq (remove #{me} players))
        current-drama @(rf/subscribe [:current-drama])
        all-hands (combine-lists player-hand cosm-hand)
        all-pools (combine-lists player-pool cosm-pool)]
    (.log js/console "other-players")
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 0 :left 0}
            :src "img/torg/logo.png" :width 250}]
     [player-display other-players 0 85 5 false]
     [player-display other-players 1 85 890 true]
     [:div {:style {:position "absolute" :top 20 :left 360}}
      [card-display "back" "img/drama/" nil {:rotation :horizontal :on-click nil}]
      [card-display current-drama "img/drama/" nil {:rotation :horizontal :on-click nil}]]
     [:div {:style {:position "absolute" :top 900 :left 20}}
      (for [[n i] (zipmap all-hands (range (count all-hands)))]
        ^{:key n} [display-all-cards n true cosm me (style-me i 220 0 true)])]
     [:div {:style {:position "absolute" :top 590 :left 20}}
      (for [[n i] (zipmap all-pools (range (count all-pools)))]
        ^{:key n} [display-all-cards n false cosm me (style-me i 220 0 true)])]
     [:div {:style {:position "absolute" :top 593 :left 207}}
      (for [[n i] (zipmap all-pools (range (count all-pools)))]
        ^{:key n} [discard-button n me (style-me i 220 0 true)])]
     [:div {:style {:position "absolute" :top 623 :left 207}}
      (for [[n i] (zipmap other-players (range (count other-players)))]
        ^{:key n} [trade-buttons n me i all-pools])]
     [:div {:style {:position "absolute" :top 200 :left 300}}
      [trade-window]]]))

(defn gm-view []
  (let [db @(rf/subscribe [:get-db])]
    (if (seq db)
      [gm-play]
      [gm-login])))

(defn player-view []
  (let [player @db/player-name]
    (if (nil? player)
      [player-login]
      [player-play])))

(defn mainview []
  (if (= "true" is-gm?)
    [:div
     [gm-view]
     [:div {:style {:position "absolute" :top 0 :left 1400}}
      [database-view]
      ]]
    [:div
     [player-view]
     [:div {:style {:position "absolute" :top 0 :left 1100}}
      ;; [database-view]
      ]]))
