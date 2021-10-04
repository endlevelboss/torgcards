(ns torgcards.logic)

(def gamestate (atom {}))

(defn sorted-random-range [amount]
  (->> (range 1 (inc amount))
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(defn deal-beginner-hand [players gamestate]
  (if-let [player (first players)]
    (let [player-destiny (take 4 (:destiny gamestate))
          new-destiny (drop 4 (:destiny gamestate))
          player-cosm (take 1 (:cosm gamestate))
          new-cosm (drop 1 (:cosm gamestate))
          new-gamestate (-> gamestate
                            (assoc player {:destiny player-destiny
                                           :cosm player-cosm})
                            (assoc :destiny new-destiny :cosm new-cosm))]
      (recur (rest players) new-gamestate))
    gamestate))

(defn initialize! [players setting]
  (let [state {:destiny (sorted-random-range 60)
               :discarded-destiny []
               :cosm (sorted-random-range 10)
               :discarded-cosm []
               :drama (sorted-random-range 40)
               :setting setting}]
    (->> (deal-beginner-hand players state)
         (reset! gamestate))))


(initialize! ["gustav" "jarl"] "nile")

@gamestate

(sorted-random-range 10)

(take 1 (range 5))
(drop 2 (range 5))

