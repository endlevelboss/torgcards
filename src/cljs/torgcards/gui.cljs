(ns torgcards.gui
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [torgcards.db :as db]
            [torgcards.ws :as ws]))

(def is-gm?
  (try
    (-> (.getElementById js/document "gm")
        .-value)
    (catch js/Object e
      "false")))


;; (defn login-enter [event value]
;;   (on-key-press event value :login)
;;   (rf/dispatch [:set-player-name value]))

(defn card-display [card path style options]
  (let [scale (if (nil? (:scale options)) 1 (:scale options))
        id (if (seqable? card) (:val card) card)
        [width height] [292 410]
        [w h] (if (= :horizontal (:rotation options)) [(* scale height) (* scale width)] [(* scale width) (* scale height)])
        my-style (assoc style :width w :height h
                        :border-radius 5 :overflow "hidden")
        myclass (if (nil? (:onclick options))
                  {:style my-style}
                  {:style my-style
                   :on-click #((:onclick options) card)})
        image (str path id ".jpg")]
    [:div myclass
     [:img {:src image
            :width w :height h
            :style {:position "relative"
                    :top 0 :left 0}}]]))

(defn player-login []
  (let [email (r/atom nil)]
    (fn []
      [:div {:style {:text-align "center"}}
       [:br]
       [:br]
       [:br]
       [:br]
       [:img {:src "img/torg/logo.png" :width 250}]
       [:br]
       [:br]
       [:label "email: "]
       [:input {:type :text
                :value @email
                :on-change #(reset! email (-> % .-target .-value))}]
       [:br]
       [:br]
       [:input {:type :button
                :value "Login"
                :on-click #(ws/login @email)}]])))



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

(defn give-card-button [[id name] text function]
  (let [value (str text name)]
    [:input {:style {:width 160 :height 80}
             :type :button
             :value value
             :on-click #(rf/dispatch [function id])}]))

(defn nameplate [name top left]
  [:div {:style {:width 100 :height 25 :background-color "lightgray"
                 :position "absolute" :top top :left left
                 :border-style "solid" :border-radius 5
                 :border-width 2 :border-color "black"
                 :text-align "center" :overflow "hidden"}}
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

(defn back-display [card left]
  (let [path (if (seqable? card)
               (str "img/cosm/" (:cosm card) "/back.jpg")
               "img/destiny/back.jpg")]
    [:img {:src path :width 100 
           :style {:position "absolute" :left (* 40 left)}}]))

(defn front-display [card left]
  (let [path (if (seqable? card)
               (str "img/cosm/" (:cosm card) "/" (:val card) ".jpg")
               (str "img/destiny/" card ".jpg"))]
    [:img {:src path :width 100
           :style {:position "absolute" :left (* 60 left)}}]))

(defn player-display-horizontal [[id name] top left]
  (let [{:keys [player-hand player-pool cosm-hand cosm-pool]} @(rf/subscribe [:player id])
        hand (into player-hand cosm-hand)
        pool (into player-pool cosm-pool)]
    [:div {:style {:position "absolute" :top top :left left}}
     [nameplate name 0 0]
     
     [:div {:style {:position "absolute" :top 32 :left 0}}
      (for [[n i] (zipmap hand (range (count hand)))]
        ^{:key i} [back-display n i])]
     [:div {:style {:position "absolute" :top 32 :left (* (count hand) 40)}}
      (for [[n i] (zipmap pool (range (count pool)))]
        ^{:key i} [front-display n i])]]))

(defn display-players [players]
  [:div
   (for [[p i] (zipmap players (range (count players)))]
     ^{:key p} [player-display-horizontal p (* i 175) 0])])

(defn cosm-select [[id name]]
  [:select {:name "cosm"
            :on-change #(rf/dispatch [:select-cosm {:player id :cosm (-> % .-target .-value)}])
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
        {:keys [id name]} @(rf/subscribe [:me])]
    ;; (.log js/console (str "id " me))
    ;; (.log js/console (str  player1))
    (if (seq trade)
      [:div {:style {:position "absolute" :top 0 :left 0 :width 400 :height 220 :background-color "lightblue"
                     :border-style "solid" :border-color "black" :border-radius 5 :border-width 3
                     :overflow "hidden" :z-index 999 }}
       [:input {:type :button :value "OK!"
                :disabled (if (nil? card2) true false)
                :style {:position "absolute" :top 10 :left 5
                        :width 80 :height 40}
                :on-click #(if (= id player1)
                             (rf/dispatch [:accept-trade])
                             nil)}]
       [:input {:type :button :value "Cancel"
                :style {:position "absolute" :top 53 :left 5
                        :width 80 :height 40}
                :on-click #(if (= id player1)
                             (rf/dispatch [:cancel-trade])
                             nil)}]
       [card-display card1 "img/destiny/" {:position "absolute" :top 5 :left 90} {:scale 0.5}]
       (if (nil? card2)
         [:img {:src "img/destiny/back.jpg" :width 146
                :style {:position "absolute" :top 5 :left 240}}]
         [card-display card2 "img/destiny/" {:position "absolute" :top 5 :left 240} {:scale 0.5}])]
      [:div])))

(defn display-drama []
  (let [cards @(rf/subscribe [:display-drama])
       {:keys [name]} @(rf/subscribe [:me])]
    [:div {:style {:position "absolute" :top 0 :left 0 :width (+ 10 (* 252 (count cards))) :height 190 
                   :background-color "lightblue"
                   :border-style "solid" :border-color "black" :border-radius 5 :border-width 3
                   :overflow "hidden" :zindex 999}}
     (for [[n i] (zipmap cards (range (count cards)))]
       ^{:key n} [:img {:src (str "img/drama/" n ".jpg") :width 250
                        :style {:position "absolute" :top 5 :left (+ 5 (* 252 i))}
                        :on-click #(if (= "gm" name)
                                     (rf/dispatch [:replace-drama n])
                                     nil)}])
     (for [[n i] (zipmap cards (range (count cards)))]
       ^{:key n} [:div {:style {:position "absolute" :top 5 :left (+ 5 (* 252 i))
                                :width 25 :height 25 :background-color "red"
                                :border-style "solid" :border-radius 15 :border-width 2 :border-color "black"
                                :text-align "center" :margin "auto" :color "white" :user-select "none"}
                        :on-click #(if (= "gm" name)
                                     (rf/dispatch [:discard-drama n])
                                     nil)}])]))


(defn extra-display [players]
  (let [drama @(rf/subscribe [:display-drama])
        trade @(rf/subscribe [:trade])]
    (cond
      (seq drama) [display-drama]
      (seq trade) [trade-window]
      :else (display-players players))))

(defn gm-play []
  (let [current @(rf/subscribe [:current-drama])
        current-drama (if (nil? current) "back" current)
        players @(rf/subscribe [:player-list])]
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 0 :left 25}
            :src "img/torg/logo.png" :width 200}]
     [:div {:style {:position "absolute" :top 380 :left 0}}
      [extra-display players]]
    ;;  [:div {:style {:position "absolute" :top 380 :left 0}}
    ;;   [display-players players]]
    ;;  [:div {:style {:position "absolute" :top 380 :left 0}}
    ;;   [display-drama]]
     [:div {:style {:position "absolute" :top 60 :left 0}}
      [:img
       {:src (str "img/drama/" current-drama ".jpg") :width 246 
        :style {:position "absolute" :top 0 :left 0}
        :on-click #(rf/dispatch [:draw-drama nil])}]
      [:img
       {:src "img/drama/back.jpg" :width 95
        :style {:position "absolute" :top 0 :left 250}
        :on-click #(rf/dispatch [:add-display-drama nil])}]
      [:input {:style {:position "absolute" :top 0 :left 350}
               :type :button
               :value "Reset"}]
      [:div {:style {:position "absolute" :top 70 :left 250}}
       (for [n players]
         ^{:key n} [give-card-button n "Destiny card: " :give-destiny-card])]
      [:div {:style {:position "absolute" :top 70 :left 415}}
       (for [n players]
         ^{:key n} [give-card-button n "Cosm card: " :give-cosm-card])]
      [:div {:style {:position "absolute" :top 70 :left 580}}
       (for [n players]
         ^{:key n} [cosm-select n])]]
    ;;  [:div {:style {:position "absolute" :top 400 :left 275}}
    ;;   [trade-window]]
     ]))


(defn move-card [name from to]
  (fn [id]
    (rf/dispatch [:move-card-from-to {:name name :id id :from from :to to}])))

(defn tag-cards [cards type]
  (map #(hash-map :value % :type type) cards))

(defn combine-lists [destinycards cosmcards]
  (let [d (tag-cards destinycards :destiny)
        c (tag-cards cosmcards :cosm)]
    (into destinycards cosmcards)))

(defn trade-card []
  (fn [id]
    (rf/dispatch [:suggest-trade id])))

(defn cardpath [card]
  (if (seqable? card)
    (str "img/cosm/" (:cosm card) "/")
    "img/destiny/"))

(defn display-all-cards [card hand? cosm player style]
  (.log js/console (seqable? card))
  (let [trade @(rf/subscribe [:trade])
        path (cardpath card)
        [hand pool] (if (seqable? card) [:cosm-hand :cosm-pool] [:player-hand :player-pool])
        [to from] (if hand? [pool hand] [hand pool])
        clickfunction (if (seq trade)
                        (cond
                          (= player (:player1 trade)) nil
                          (and (= player (:player2 trade))
                               (not hand?)) (trade-card)
                          (and (= player (:player2 trade))
                               hand?) nil
                          :else (move-card player from to))
                        (move-card player from to))]
    [card-display card path style {:scale 0.75 :onclick clickfunction}]))

(defn discard-button [card name style]
  (let [s (assoc style :width 25 :height 25 :background-color "red"
                 :border-style "solid" :border-radius 15 :border-width 2 :border-color "black"
                 :text-align "center" :margin "auto" :color "white" :user-select "none")
        {:keys [player1 player2]} @(rf/subscribe [:trade])]
    (if (or (= name player1) (= name player2))
      [:div]
      [:div {:style s
             :on-click #(if (seqable? card)
                          (rf/dispatch [:discard-cosm {:player name :id card}])
                          (rf/dispatch [:discard-destiny {:player name :id card}]))}
       "X"])))

(defn trade-button [card me [id name] style indx]
  (if (seqable? card)
    [:div]
    (let [top (* indx 30)
          s (assoc style :width 25 :height 25 :background-color "darkgreen"
                   :border-style "solid" :border-radius 15 :border-width 2 :border-color "black"
                   :text-align "center" :margin "auto" :color "white" :user-select "none" :overflow "hidden"
                   :top top)]
      [:div {:style s
             :on-click #(rf/dispatch [:trade-card {:player1 me :player2 id :card1 card}])}
       name])))

(defn trade-buttons [player me indx pools]
  (let [trade? (seq @(rf/subscribe [:trade]))]
    (if trade?
      [:div]
      [:div
       (for [[n i] (zipmap pools (range (count pools)))]
         ^{:key n} [trade-button n me player (style-me i 220 0 true) indx])])))

(defn player-background [hand pool]
  (let [cards (max 5 (count hand) (count pool))]
    [:div {:style {:position "absolute" :top 0 :left 0 :width (+ 20 (* 220 cards)) :height 680 :background-color "lightblue"
                   :border-style "solid" :border-color "black" :border-radius 5 :border-width 3
                   :overflow "hidden" :zindex 999}}
     [:div {:style {:position "absolute" :top 100 :left 100 :color "darkgrey" :font-size "120px"
                    :font-family "Garamond, serif"}}
      "POOL"]
     [:div {:style {:position "absolute" :top 430 :left 100 :color "darkgrey" :font-size "120px"
                    :font-family "Garamond, serif"}}
      "HAND"]]))

(defn blue-stripe [hand pool]
  (let [cards (max 5 (count hand) (count pool))]
    [:div {:style {:width (+ 20 (* 220 cards)) :height 48 :background-color "cadetblue"}}]))


(defn player-play []
  (let [{:keys [id name] :as me} @(rf/subscribe [:me])
        {:keys [player-hand player-pool cosm-hand cosm-pool cosm]} @(rf/subscribe [:player id])
        w-height @(rf/subscribe [:window-height])
        phand-top (max 250 (- w-height 693))
        players @(rf/subscribe [:player-list])
        other-players (seq (remove #{[id name]} players))
        current-drama @(rf/subscribe [:current-drama])
        all-hands (into player-hand cosm-hand)
        all-pools (into player-pool cosm-pool)]
    (.log js/console w-height)
    [:div {:style {:position "absolute"}}
     [:div {:style {:position "absolute" :top 0 :left 0}}
      [:img {:style {:position "absolute" :top 0 :left 25}
             :src "img/torg/logo.png" :width 200}]
      [:div {:style {:position "absolute" :top 60 :left 0}}
       [card-display current-drama "img/drama/" nil {:rotation :horizontal :on-click nil
                                                     :scale 0.6}]]]
     [:div {:style {:position "absolute" :top phand-top :left 0}}
      [:div {:style {:position "absolute" :top -10 :left 10}
             :zindex -1}
       [player-background all-hands all-pools]]
      [:div {:style {:position "absolute" :top 0 :left 20}}
       (for [[n i] (zipmap all-pools (range (count all-pools)))]
         ^{:key n} [display-all-cards n false cosm id (style-me i 220 0 true)])]
      [:div {:style {:position "absolute" :top 310 :left 13}}
       [blue-stripe all-hands all-pools]]
      [:div {:style {:position "absolute" :top 310 :left 90}}
       [:img {:src "img/torg/bonuschart.png" :height 47}]]
      [:div {:style {:position "absolute" :top 360 :left 20}}
       (for [[n i] (zipmap all-hands (range (count all-hands)))]
         ^{:key n} [display-all-cards n true cosm id (style-me i 220 0 true)])]
      [:div {:style {:position "absolute" :top 3 :left 207}}
       (for [[n i] (zipmap all-pools (range (count all-pools)))]
         ^{:key n} [discard-button n id (style-me i 220 0 true)])]
      [:div {:style {:position "absolute" :top 33 :left 207}}
       (for [[n i] (zipmap other-players (range (count other-players)))]
         ^{:key n} [trade-buttons n id i all-pools])]]
     [:div {:style {:position "absolute" :top 0 :left 300}}
      [extra-display other-players]]
    ;;  [:div {:style {:position "absolute" :top 0 :left 300}}
    ;;   [trade-window]]
     ]))


(defn player-view []
  (let [me @(rf/subscribe [:me])]
    (case (:name me)
      nil [player-login]
      "gm" [gm-play]
      [player-play])))

(defn mainview []
  [:div
   [player-view]
  ;;  [:div {:style {:position "absolute" :top 0 :left 1100}}
  ;;   [database-view]]
   ])
