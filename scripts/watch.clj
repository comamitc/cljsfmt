(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'cljsfmt.core
   :output-to "out/cljsfmt.js"
   :output-dir "out"})
