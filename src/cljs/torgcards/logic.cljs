(ns torgcards.logic)

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

(def initial-db
  {:destiny (sorted-random-range 60)
   :discarded-destiny []
   :cosm (sorted-random-range 10)
   :discarded-cosm []
   :drama (sorted-random-range 40)
   :setting nil})