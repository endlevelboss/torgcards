(ns torgcards.gui
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [torgcards.ws :as ws]))

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

(defn give-card-button [[id name] text function]
  (let [myname @(rf/subscribe [:player-name name])
        value (str text myname)]
    [:input {:style {:width 160 :height 80}
             :type :button
             :value value
             :on-click #(rf/dispatch [function id])}]))

(defn nameplate [name top left]
  (let [myname @(rf/subscribe [:player-name name])]
    [:div {:style {:width 100 :height 25 :background-color "lightgray"
                   :position "absolute" :top top :left left
                   :border-style "solid" :border-radius 5
                   :border-width 2 :border-color "black"
                   :text-align "center" :overflow "hidden"}}
     myname]))

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
           :style {:position "absolute" :left (* 60 left)}
           :on-mouse-enter #(rf/dispatch [:set-magnify card])
           :on-mouse-leave #(rf/dispatch [:set-magnify nil])}]))

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

(defn display-magnified []
  (let [card @(rf/subscribe [:magnify])
        card-info (if (seqable? card)
                    (str "img/cosm/" (:cosm card) "/" (:val card) ".jpg")
                    (str "img/destiny/" card ".jpg"))]
    (if (nil? card)
      [:div]
      [:img {:src card-info :width 219}])))

(defn display-players [players]
  [:div
   (for [[p i] (zipmap players (range (count players)))]
     ^{:key p} [player-display-horizontal p (* i 175) 0])
   [:div {:style {:position "absolute" :top 0 :left 500}}
    [display-magnified]]])

(defn cosm-select [[id _]]
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

(defn set-cosm-tent []
  [:select {:name "cosm"
            :on-change #(rf/dispatch [:set-cosm-tent {:cosm (-> % .-target .-value)}])
            :style {:width 120 :height 30}}
   [:option {:value "nil"} "Select cosm-tent:"]
   [:option {:value "aysle"} "Aysle"]
   [:option {:value "earth"} "Core Earth"]
   [:option {:value "cyberpapacy"} "Cyberpapacy"]
   [:option {:value "livingland"} "Living Land"]
   [:option {:value "nile"} "Nile Empire"]
   [:option {:value "orrorsh"} "Orrorsh"]
   [:option {:value "panpacifica"} "Pan-Pacifica"]
   [:option {:value "tharkold"} "Tharkold"]])

(defn axiom-box [type value]
  [:div {:style {:position "relative" :border-width 3
                 :border-radius 5 :background-color "white"
                 :width 80 :height 80 :border-color "black"
                 :border-style "solid" :text-align "center"}}
   [:div {:style {:position "relative" :top 2}} 
    type]
   [:h1 value]])

(defn effect-box [title text]
  [:div {:style {:border-width 3 :border-style "solid"
                 :border-color "black" :width 165 :height 100
                 :border-radius 5 :background-color "white"
                 :text-align "center" :overflow "hidden"}}
   [:b title]
   [:br]
   text])

(defn tent []
  (let [tentdata @(rf/subscribe [:tent])]
    (.log js/console (str tentdata))
    [:div {:style {:position "relative" :border-width 3
                   :border-style "solid" :border-radius 5
                   :border-color "black" :width 365 :height 300
                   :text-align "center" :background-color "lightblue"
                   :overflow "hidden"}}
     [:h2 (:name tentdata)]
     [:div {:style {:position "absolute" :top 50 :left 5}}
      [axiom-box "Magic" (:magic tentdata)]]
     [:div {:style {:position "absolute" :top 50 :left 95}}
      [axiom-box "Social" (:social tentdata)]]
     [:div {:style {:position "absolute" :top 50 :left 185}}
      [axiom-box "Spirit" (:spirit tentdata)]]
     [:div {:style {:position "absolute" :top 50 :left 275}}
      [axiom-box "Tech" (:tech tentdata)]]
     [:br]
     [:br]
     [:br]
     [:br]
     [:h3 (:law tentdata)]
     [:div {:style {:position "absolute" :top 190 :left 5}}
      [effect-box (:effectname1 tentdata) (:effect1 tentdata)]]
     [:div {:style {:position "absolute" :top 190 :left 190}}
      [effect-box (:effectname2 tentdata) (:effect2 tentdata)]]]))

;; (defn trade-window []
;;   (let [{:keys [player1 _ card1 card2] :as trade} @(rf/subscribe [:trade])
;;         {:keys [id _]} @(rf/subscribe [:me])]
;;     (if (seq trade)
;;       [:div {:style {:position "absolute" :top 0 :left 0 :width 400 :height 220 :background-color "lightblue"
;;                      :border-style "solid" :border-color "black" :border-radius 5 :border-width 3
;;                      :overflow "hidden" :z-index 999 }}
;;        [:input {:type :button :value "OK!"
;;                 :disabled (if (nil? card2) true false)
;;                 :style {:position "absolute" :top 10 :left 5
;;                         :width 80 :height 40}
;;                 :on-click #(if (= id player1)
;;                              (rf/dispatch [:accept-trade])
;;                              nil)}]
;;        [:input {:type :button :value "Cancel"
;;                 :style {:position "absolute" :top 53 :left 5
;;                         :width 80 :height 40}
;;                 :on-click #(if (= id player1)
;;                              (rf/dispatch [:cancel-trade])
;;                              nil)}]
;;        [card-display card1 "img/destiny/" {:position "absolute" :top 5 :left 90} {:scale 0.5}]
;;        (if (nil? card2)
;;          [:img {:src "img/destiny/back.jpg" :width 146
;;                 :style {:position "absolute" :top 5 :left 240}}]
;;          [card-display card2 "img/destiny/" {:position "absolute" :top 5 :left 240} {:scale 0.5}])]
;;       [:div])))

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
     (if (= "gm" name)
       (for [[n i] (zipmap cards (range (count cards)))]
         ^{:key n} [:img {:style {:position "absolute" :top 5 :left (+ 5 (* 252 i))
                                  :user-select "none"}
                          :src "img/torg/discard.png" :width 35
                          :on-click #(rf/dispatch [:discard-drama n])}])
       nil)]))


(defn extra-display [players]
  (let [drama @(rf/subscribe [:display-drama])]
    (cond
      (seq drama) [display-drama]
      :else (display-players players))))

(defn gm-play []
  (let [current @(rf/subscribe [:current-drama])
        current-drama (if (nil? current) "back" current)
        players @(rf/subscribe [:player-list])]
    [:div {:style {:position "absolute"}}
     [:img {:style {:position "absolute" :top 0 :left 25}
            :src "img/torg/logo.png" :width 200}]
     [:img {:style {:position "absolute" :top 60 :left 450}
            :src "img/torg/bonuschart.png" :height 60}]
     [:div {:style {:position "absolute" :top 380 :left 0}}
      [extra-display players]]
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
      [:div {:style {:position "absolute" :top 70 :left 750}}
       [set-cosm-tent]]
      [:div {:style {:position "absolute" :top 100 :left 750}}
       [tent]]
      [:div {:style {:position "absolute" :top 70 :left 250}}
       (for [n players]
         ^{:key n} [give-card-button n "Destiny card: " :give-destiny-card])]
      [:div {:style {:position "absolute" :top 70 :left 415}}
       (for [n players]
         ^{:key n} [give-card-button n "Cosm card: " :give-cosm-card])]
      [:div {:style {:position "absolute" :top 70 :left 580}}
       (for [n players]
         ^{:key n} [cosm-select n])]]]))


(defn move-card [name from to]
  (fn [id]
    (rf/dispatch [:move-card-from-to {:name name :id id :from from :to to}])))

(defn trade-card []
  (fn [id]
    (rf/dispatch [:suggest-trade id])))

(defn cardpath [card]
  (if (seqable? card)
    (str "img/cosm/" (:cosm card) "/")
    "img/destiny/"))

(defn display-all-cards [card hand? cosm player style]
  (.log js/console (seqable? card))
  (let [path (cardpath card)
        [hand pool] (if (seqable? card) [:cosm-hand :cosm-pool] [:player-hand :player-pool])
        [to from] (if hand? [pool hand] [hand pool])]
    [card-display card path style {:scale 0.75 :onclick (move-card player from to)}]))

(defn discard-button [card name style]
  [:img {:style style
         :on-click #(if (seqable? card)
                      (rf/dispatch [:discard-cosm {:player name :id card}])
                      (rf/dispatch [:discard-destiny {:player name :id card}]))
         :src "img/torg/discard.png" :width 35}])

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
  (let [{:keys [id name]} @(rf/subscribe [:me])
        {:keys [player-hand player-pool cosm-hand cosm-pool cosm]} @(rf/subscribe [:player id])
        w-height @(rf/subscribe [:window-height])
        phand-top (max 250 (- w-height 693))
        players @(rf/subscribe [:player-list])
        other-players (seq (remove #{[id name]} players))
        current @(rf/subscribe [:current-drama])
        current-drama(if (nil? current) "back" current)
        all-hands (into player-hand cosm-hand)
        all-pools (into player-pool cosm-pool)]
    [:div {:style {:position "absolute"}}
     [:div {:style {:position "absolute" :top 0 :left 0}}
      [:img {:style {:position "absolute" :top 0 :left 25}
             :src "img/torg/logo.png" :width 200}]
      [:img
       {:src (str "img/drama/" current-drama ".jpg") :width 246
        :style {:position "absolute" :top 60 :left 0}}]]
     [:div {:style {:position "absolute" :top 250 :left 0}}
      [tent]]
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
      [:div {:style {:position "absolute" :top 3 :left 201}}
       (for [[n i] (zipmap all-pools (range (count all-pools)))]
         ^{:key n} [discard-button n id (style-me i 220 0 true)])]
      ;; [:div {:style {:position "absolute" :top 33 :left 207}}
      ;;  (for [[n i] (zipmap other-players (range (count other-players)))]
      ;;    ^{:key n} [trade-buttons n id i all-pools])]
      ]
     [:div {:style {:position "absolute" :top 0 :left 400}}
      [extra-display other-players]]
     ]))


(defn player-view []
  (let [me @(rf/subscribe [:me])]
    (case (:name me)
      nil [player-login]
      "gm" [gm-play]
      [player-play])))

(defn mainview []
  [:div
   [player-view]])
