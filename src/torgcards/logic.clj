(ns torgcards.logic)

(defn sorted-random-range [amount]
  (->> (range 1 (inc amount))
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(defn initial-db []
  {:destiny (sorted-random-range 60)
   :discarded-destiny []
   :cosm (sorted-random-range 10)
   :discarded-cosm []
   :drama (sorted-random-range 40)
   :current-drama nil
   :player-list #{}})

(defn draw-drama-card [db _]
  (let [drama (if (seq (:drama db)) (:drama db) (sorted-random-range 40))
        new-card (first drama)
        new-deck (rest drama)]
   (assoc db :current-drama new-card :drama new-deck)))

(defn shuffle-discarded [db pile]
  (->> (pile db)
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(defn shuffle-discarded-destiny [db]
  (shuffle-discarded db :discarded-destiny))

(defn shuffle-discarded-cosm [db]
  (shuffle-discarded db :discarded-cosm))

(defn deal-destiny-card [db name]
  (let [not-empty-destiny? (seq (:destiny db))
        destiny (if not-empty-destiny? (:destiny db) (shuffle-discarded-destiny db))
        discard (if not-empty-destiny? (:discarded-destiny db) [])
        players (update-in (:players db) [name :player-hand] conj (first destiny))]
   (assoc db :destiny (rest destiny) :discarded-destiny discard :players players)))

(defn deal-cosm-card [db name]
  (let [not-empty-cosm? (seq (:cosm db))
        cosm (if not-empty-cosm? (:cosm db) (shuffle-discarded-cosm db))
        discard (if not-empty-cosm? (:discarded-cosm db) [])
        players (update-in (:players db) [name :cosm-hand] conj (first cosm))]
   (assoc db :cosm (rest cosm) :discarded-cosm discard :players players)))

(defn set-cosm-for-player [db {:keys [player cosm]}]
  (let [players (assoc-in (:players db) [player :cosm] cosm)]
   (assoc db :players players)))

(defn move-card [db {:keys [name id from to] :as v}]
  (let [to-arr (conj (get-in db [:players name to]) id)
        from-arr (into [] (remove #{id} (get-in db [:players name from])))]
   (assoc db :players (-> (:players db)
                          (assoc-in [name from] from-arr)
                          (assoc-in [name to] to-arr)))))

(defn discard-card [db {:keys [id player]}]
  (let [pool (into [] (remove #{id} (get-in db [:players player :player-pool])))
        discard (conj (:discarded-destiny db) id)]
   (assoc db :players (-> (:players db)
                          (assoc-in [player :player-pool] pool))
          :discarded-destiny discard)))

(defn discard-cosm [db {:keys [id player]}]
  (let [pool (into [] (remove #{id} (get-in db [:players player :cosm-pool])))
        discard (conj (:discarded-cosm db) id)]
   (assoc db :players (-> (:players db)
                          (assoc-in [player :cosm-pool] pool))
          :discarded-cosm discard)))

(defn add-card-to-trade [db id]
  (assoc-in db [:trade :card2] id))

(defn swap-cards [db player1 card1 card2]
  (-> (into [] (remove #{card1} (get-in db [:players player1 :player-pool])))
      (conj card2)))

(defn accept-trade [db _]
  (let [{:keys [player1 player2 card1 card2]} (:trade db)
        pool1 (swap-cards db player1 card1 card2)
        pool2 (swap-cards db player2 card2 card1)]
    (-> db
        (assoc-in [:players player1 :player-pool] pool1)
        (assoc-in [:players player2 :player-pool] pool2)
        (assoc-in [:trade] {}))))

(defn cancel-trade [db _]
  (assoc db :trade {}))

(defn start-trade [db value]
  (assoc db :trade value))