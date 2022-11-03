(ns torgcards.data)

(def tentdata
  {nil nil
   "livingland" {:name "The Living Land"
                 :magic 1 :social 7 :spirit 24 :tech 6
                 :law "Law of Wonder"
                 :effectname1 "Savagery"
                 :effect1 "When Attack is an Approved Action, All-Out Attacks gain +1 BD"
                 :effectname2 "Life"
                 :effect2 "Gain an Up when testing for Defeat"}
   "earth" {:name "Core Earth"
            :magic 9 :social 23 :spirit 10 :tech 23
            :law "Law of Hope"
            :effectname1 "Glory"
            :effect1 "Play for the Critical Moment to swap in a Glory card from the deck."
            :effectname2 "Underdog"
            :effect2 "Start with a card in your Action Pool when at a disadvantage"}
   "aysle" {:name "Aysle"
            :magic 24 :social 16 :spirit 18 :tech 14
            :law "Law of Light & Darkness"
            :effectname1 "Magic"
            :effect1 "Spend Shock to gain +1 on a magic skill test."
            :effectname2 "Enchantment"
            :effect2 "An item becomes magical when a character rolls 60+ on a test."}
   "cyberpapacy" {:name "The Cyberpapacy"
                  :magic 14 :social 18 :spirit 16 :tech 26
                  :law "Law of Suspicion"
                  :effectname1 "One True Way"
                  :effect1 "Religions other than the Cyberchurch are contradictions."
                  :effectname2 "Heretical Magic"
                  :effect2 "Mishaps on magic causes an extra 1 BD Shock."}
   "nile" {:name "The Nile Empire"
           :magic 14 :social 20 :spirit 18 :tech 20
           :law "Law of Drama"
           :effectname1 "Heroism"
           :effect1 "Storm Knight Destiny card hans size increases by one."
           :effectname2 "Action"
           :effect2 "Spend a second Possibility to roll another die for a test, and take the better of the two."}
   "orrorsh" {:name "Orrorsh"
              :magic 16 :social 18 :spirit 16 :tech 18
              :law "Law of Fear"
              :effectname1 "Eternal Corruption"
              :effect1 "When noted, roll willpower or Spirit or lose one Charisma."
              :effectname2 "Perseverance"
              :effect2 "A standard Soak does'n stop Wounds, Good negates one Wound, and Outstanding negates all."}
   "panpacifica" {:name "Pan-Pacifica"
                  :magic 4 :social 24 :spirit 8 :tech 24
                  :law "Law of Intrigue"
                  :effectname1 "Tenacity"
                  :effect1 "Suffer a Wound to eliminate all of your Shock."
                  :effectname2 "Vengeance"
                  :effect2 "Your Destiny hand size increases by one for an act once a wrong is avenged."}
   "tharkold" {:name "Tharkold"
               :magic 12 :social 25 :spirit 4 :tech 25
               :law "Law of Ferocity"
               :effectname1 "Domination"
               :effect1 "When intimidation is approved draw two Destiny cards and choose which to keep."
               :effectname2 "Pain"
               :effect2 "Recover one Shock when you deal one or more Wounds."}})


