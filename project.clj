(defproject torgcards "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
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
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]]
  :repl-options {:init-ns torgcards.core}
  :main ^:skip-aot torgcards.core

  
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s"

  :plugins [[lein-cljsbuild "1.1.8"]]

  :cljsbuild
  {:builds
   {:app {:source-paths ["src/cljs"]
          :compiler {:output-to "target/cljsbuild/public/js/app.js"
                     :output-dir "target/cljsbuild/public/js/out"
                     :main "torgcards.core"
                     :asset-path "/js/out"
                     :optimizations :none
                     :source-map true
                     :pretty-print true}}}}
  :clean-targets
  ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]
  
  :profiles {:dev {:resource-paths ["target"]
                   :clean-targets ^{:protect false} ["target"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
