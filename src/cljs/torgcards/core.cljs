(ns torgcards.core
  (:require [cljs.reader :as edn]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [torgcards.gui :as gui]
            [torgcards.db :as db]))



(defn connect! [url receive-handler]
  (if-let [chan (js/WebSocket. url)]
    (do (.log js/console "Connected!")
        (set! (.-onmessage chan)
              #(->> %
                    .-data
                    edn/read-string
                    receive-handler))
        (reset! db/channel chan))
    (throw (js/Error. "Shite!"))))




(defn mount []
  (rd/render
   [gui/mainview]
   (.getElementById js/document "content")))

;; (-> (.getElementById js/document "content")
;;     (.-innerHTML)
;;     (set! "Hellois Torg-World"))

(mount)

(connect! (str "ws://" (.-host js/location) "/ws") db/handle-response!)