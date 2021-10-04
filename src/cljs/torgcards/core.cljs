(ns torgcards.core
  (:require [cljs.reader :as edn]))

(defonce channel (atom nil))

(defn connect! [url receive-handler]
  (if-let [chan (js/WebSocket. url)]
    (do (.log js/console "Connected!")
        (reset! channel chan))
    (throw (js/Error. "Shite!"))))

(defn handle-response [response]
  (.log js/console response))

(-> (.getElementById js/document "content")
    (.-innerHTML)
    (set! "Hellois Torg-World"))

(connect! (str "ws://" (.-host js/location) "/ws") handle-response)