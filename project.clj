(defproject cljsfmt "0.1.0"
  :description "A ClojureScript port of cljfmt"
  :url "https://github.com/comamitc/cljsfmt"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [rewrite-cljs "0.4.0"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"] [lein-cljsbuild "1.1.2"]]
  :npm {:dependencies [[source-map-support "0.4.0"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release"]
  :target-path "target"
  :cljsbuild {
    :builds [{:id "test"
              :source-paths ["src" "test"]
              :notify-command ["phantomjs" "phantom/unit-test.js" "phantom/unit-test.html"]
              :compiler { :optimizations :whitespace
                          :pretty-print true
                          :output-to "target/testable.js"}}
              {:id "dev"
                :source-paths ["src"]
                :compiler { :optimizations :whitespace
                            :output-to "out/cljsfmt"
                            :output-dir "out/"
                            :pretty-print true}}]})
