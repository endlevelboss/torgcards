(ns torgcards.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [torgcards.db :as db]
            [torgcards.gui :as gui]))




(defn inner-height []
  (db/set-window-height! (. js/window -innerHeight)))

(defn mount []
  (.addEventListener js/window "resize" inner-height)
  (inner-height)
  (rd/render
   [gui/mainview]
   (.getElementById js/document "content")))

;; (-> (.getElementById js/document "content")
;;     (.-innerHTML)
;;     (set! "Hellois Torg-World"))

(mount)



