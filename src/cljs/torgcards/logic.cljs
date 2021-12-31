(ns torgcards.logic)

(defn sorted-random-range [amount]
  (->> (range 1 (inc amount))
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(def initial-db
  {:destiny (sorted-random-range 60)
   :discarded-destiny []
   :cosm (sorted-random-range 10)
   :discarded-cosm []
   :drama (sorted-random-range 40)
   :current-drama nil
   :setting nil})

(defn shuffle-discarded [db pile]
  (->> (pile db)
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(defn shuffle-discarded-destiny [db]
  (shuffle-discarded db :discarded-destiny))

(defn shuffle-discarded-cosm [db]
  (shuffle-discarded db :discarded-cosm))

(defn deal-destiny-card [name db]
  (let [not-empty-destiny? (seq (:destiny db))
        destiny (if not-empty-destiny? (:destiny db) (shuffle-discarded-destiny db))
        discard (if not-empty-destiny? (:discarded-destiny db) [])
        players (update-in (:players db) [name :player-hand] conj (first destiny))]
    {:destiny (rest destiny) :discarded-destiny discard :players players}))

(defn deal-cosm-card [name db]
  (let [not-empty-cosm? (seq (:cosm db))
        cosm (if not-empty-cosm? (:cosm db) (shuffle-discarded-cosm db))
        discard (if not-empty-cosm? (:discarded-cosm db) [])
        players (update-in (:players db) [name :cosm-hand] conj (first cosm))]
    {:cosm (rest cosm) :discarded-cosm discard :players players}))

(defn set-cosm-for-player [name cosm db]
  (let [players (assoc-in (:players db) [name :cosm] cosm)]
    {:players players}))

(defn discard-card [id player db]
  (let [pool (into [] (remove #{id} (get-in db [:players player :player-pool])))
        discard (conj (:discarded-destiny db) id)]
   {:players (-> (:players db)
                 (assoc-in [player :player-pool] pool))
    :discarded-destiny discard}))

(defn discard-cosm [id player db]
  (let [pool (into [] (remove #{id} (get-in db [:players player :cosm-pool])))
        discard (conj (:discarded-cosm db) id)]
    {:players (-> (:players db)
                  (assoc-in [player :cosm-pool] pool))
     :discarded-cosm discard}))

(defn move-card [db player id from to]
  (let [to-arr (conj (get-in db [:players player to]) id)
        from-arr (into [] (remove #{id} (get-in db [:players player from])))]
    {:players (-> (:players db)
                  (assoc-in [player from] from-arr)
                  (assoc-in [player to] to-arr))}))