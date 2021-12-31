(ns torgcards.logic)

(def cosms ["aysle" "earth" "cyberpapacy" "livingland" "nile" "orrorsh" "panpacifica" "tharkold"])

(defn sorted-random-range [amount]
  (->> (range 1 (inc amount))
       (map #(hash-map :val % :rnd (rand)))
       (sort-by :rnd)
       (map :val)))

(defn random-cosm [cosm]
  (->> (range 1 11)
       (map #(hash-map :val % :rnd (rand) :cosm cosm))))

(defn generate-cosms []
  (reduce #(into %1 (random-cosm %2)) #{} cosms))

(defn initial-db []
  {:destiny (sorted-random-range 60)
   :discarded-destiny []
   :cosm (generate-cosms)
   :discarded-cosm []
   :drama (sorted-random-range 40)
   :current-drama nil})

(defn draw-drama [db]
  (let [drama (if (seq (:drama db)) (:drama db) (sorted-random-range 40))
        new-card (first drama)
        new-deck (rest drama)]
    [new-card new-deck]))

(defn draw-drama-card [db _]
  (let [[card deck] (draw-drama db)]
    (assoc db :current-drama card :drama deck)))

(defn add-display-drama [db _]
  (let [[card deck] (draw-drama db)]
    (-> db
        (update-in [:display-drama] conj card)
        (assoc :drama deck))))

(defn discard-display [db card]
  (let [deck (remove #{card} (:display-drama db))]
    (assoc db :display-drama deck)))

(defn replace-drama [db card]
  (let [display-deck (remove #{card} (:display-drama db))
        drama-deck  (into [card] (:drama db))]
    (assoc db :drama drama-deck :display-drama display-deck)))

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

(defn reshuffle-discarded [db cosm]
  (let [discarded (filter #(= cosm (:cosm %)) (:discarded-cosm db))
        reshuffled (map #(assoc % :rnd (rand)) discarded)]
    (assoc db
           :discarded-cosm (apply disj (into #{} (:discarded-cosm db)) discarded)
           :cosm (into (:cosm db) reshuffled))))

(defn filter-cosm-cards [db cosm]
  (filter #(= cosm (:cosm %)) (:cosm db)))

(defn get-cosm-cards [db cosm]
  (let [not-empty? (seq (filter-cosm-cards db cosm))]
    (if not-empty?
      db
      (reshuffle-discarded db cosm))))

(defn select-cosm-card [db cosm]
  (->> (filter #(= cosm (:cosm %)) (:cosm db))
       (sort-by :rnd)
       first))

(defn deal-cosm-card [db name]
  (let [cosm-name (get-in db [:players name :cosm])
        new-db (get-cosm-cards db cosm-name)
        card (select-cosm-card new-db cosm-name)]
    (assoc new-db 
           :cosm (disj (:cosm new-db) card) 
           :players (update-in (:players new-db) [name :cosm-hand] conj card))))

(defn set-cosm-for-player [db {:keys [player cosm]}]
  (let [players (assoc-in (:players db) [player :cosm] cosm)]
    (assoc db :players players)))

(defn move-card [db {:keys [name id from to] :as v}]
  (println v)
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

