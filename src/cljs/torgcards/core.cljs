(ns torgcards.core
  (:require [reagent.dom :as rd]
            [re-frame.core :as rf]
            [torgcards.db :as db]
            [torgcards.gui :as gui]))




(defn inner-height []
  (rf/dispatch [:set-window-height (. js/window -innerHeight)]))

(defn mount []
  (.addEventListener js/window "resize" inner-height)
  (inner-height)
  (rd/render
   [gui/mainview]
   (.getElementById js/document "content")))

(mount)



