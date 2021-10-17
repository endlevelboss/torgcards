(ns torgcards.web
  (:require [torgcards.core :as core]
            [environ.core :refer [env]])
  (:gen-class))


(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (core/start port)))