(defproject torgcards "1.0.0"
  :description "Cardhandler for Torg Eternity"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.11"]
                 [metosin/ring-http-response "0.9.1"]
                 [http-kit "2.5.3"]
                 [ring "1.8.2"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "1.10.764"]
                 [reagent "1.1.0"]
                 [re-frame "1.2.0"]
                 [environ "0.5.0"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]]
  :min-lein-version "2.4.0"
  :repl-options {:init-ns torgcards.core}
  ;; :main ^:skip-aot torgcards.core


  :resource-paths ["resources"]
  :target-path "target/%s"

  :plugins [[lein-cljsbuild "1.1.8"]
            [environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]

  :cljsbuild
  {:builds
   {:app {:source-paths ["src/cljs"]
          :compiler {:output-to "resources/public/js/app.js"
                     :output-dir "resources/public/js/out"
                     :main "torgcards.core"
                     :asset-path "/js/out"
                     :optimizations :none
                     :source-map true
                     :pretty-print true}}
    :prod {:source-paths ["src/cljs"]
           :compiler {:output-to "resources/public/js/app.js"
                      :main "torgcards.core"
                      :optimizations :advanced}}}}
  ;; :clean-targets
  ;; ^{:protect false}
  ;; [:target-path
  ;;  [:cljsbuild :builds :app :compiler :output-dir]
  ;;  [:cljsbuild :builds :app :compiler :output-to]]

  :uberjar-name "torgcards.jar"

  :profiles {:dev {:resource-paths ["target"]
                   :clean-targets ^{:protect false} ["target"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
