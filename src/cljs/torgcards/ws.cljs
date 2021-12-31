(ns torgcards.ws
  (:require [cljs.reader :as edn]
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

(defn login [email]
  (let [url (str "ws://" (.-host js/location) "/user/" email)]
    (.log js/console url)
    (connect!
     url
     db/handle-response!)))