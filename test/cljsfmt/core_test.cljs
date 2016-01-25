(ns cljsfmt.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [cljsfmt.core :as f]))

; (deftest test-indent
;   (testing "list indentation"
;     (is (= (f/reformat-string "(foo bar\nbaz\nquz)")
;            "(foo bar\n     baz\n     quz)"))
;     (is (= (f/reformat-string "(foo\nbar\nbaz)")
;            "(foo\n bar\n baz)")))

;   (testing "block indentation"
;     (is (= (f/reformat-string "(if (= x 1)\n:foo\n:bar)")
;            "(if (= x 1)\n  :foo\n  :bar)"))
;     (is (= (f/reformat-string "(do\n(foo)\n(bar))")
;            "(do\n  (foo)\n  (bar))"))
;     (is (= (f/reformat-string "(do (foo)\n(bar))")
;            "(do (foo)\n    (bar))"))
;     (is (= (f/reformat-string "(deftype Foo\n[x]\nBar)")
;            "(deftype Foo\n         [x]\n  Bar)"))
;     (is (= (f/reformat-string "(cond->> x\na? a\nb? b)")
;            "(cond->> x\n  a? a\n  b? b)")))

;   (testing "constant indentation"
;     (is (= (f/reformat-string "(def foo\n\"Hello World\")")
;            "(def foo\n  \"Hello World\")"))
;     (is (= (f/reformat-string "(defn foo [x]\n(+ x 1))")
;            "(defn foo [x]\n  (+ x 1))"))
;     (is (= (f/reformat-string "(defn foo\n[x]\n(+ x 1))")
;            "(defn foo\n  [x]\n  (+ x 1))"))
;     (is (= (f/reformat-string "(defn foo\n([] 0)\n([x]\n(+ x 1)))")
;            "(defn foo\n  ([] 0)\n  ([x]\n   (+ x 1)))"))
;     (is (= (f/reformat-string "(fn [x]\n(foo bar\nbaz))")
;            "(fn [x]\n  (foo bar\n       baz))"))
;     (is (= (f/reformat-string "(fn [x] (foo bar\nbaz))")
;            "(fn [x] (foo bar\n             baz))")))

;   (testing "inner indentation"
;     (is (= (f/reformat-string "(letfn [(foo [x]\n(* x x))]\n(foo 5))")
;            "(letfn [(foo [x]\n          (* x x))]\n  (foo 5))"))
;     (is (= (f/reformat-string "(reify Closeable\n(close [_]\n(prn :closed)))")
;            "(reify Closeable\n  (close [_]\n    (prn :closed)))"))
;     (is (= (f/reformat-string "(defrecord Foo [x]\nCloseable\n(close [_]\n(prn x)))")
;            "(defrecord Foo [x]\n  Closeable\n  (close [_]\n    (prn x)))")))

;   (testing "data structure indentation"
;     (is (= (f/reformat-string "[:foo\n:bar\n:baz]")
;            "[:foo\n :bar\n :baz]"))
;     (is (= (f/reformat-string "{:foo 1\n:bar 2}")
;            "{:foo 1\n :bar 2}"))
;     (is (= (f/reformat-string "#{:foo\n:bar\n:baz}")
;            "#{:foo\n  :bar\n  :baz}"))
;     (is (= (f/reformat-string "{:foo [:bar\n:baz]}")
;            "{:foo [:bar\n       :baz]}")))

;   (testing "embedded structures"
;     (is (= (f/reformat-string "(let [foo {:x 1\n:y 2}]\n(:x foo))")
;            "(let [foo {:x 1\n           :y 2}]\n  (:x foo))"))
;     (is (= (f/reformat-string "(if foo\n(do bar\nbaz)\nquz)")
;            "(if foo\n  (do bar\n      baz)\n  quz)")))

;   (testing "namespaces"
;     (is (= (f/reformat-string "(t/defn foo [x]\n(+ x 1))")
;            "(t/defn foo [x]\n  (+ x 1))"))
;     (is (= (f/reformat-string "(t/defrecord Foo [x]\nCloseable\n(close [_]\n(prn x)))")
;            "(t/defrecord Foo [x]\n  Closeable\n  (close [_]\n    (prn x)))")))

;   (testing "function #() syntax"
;     (is (= (f/reformat-string "#(while true\n(println :foo))")
;            "#(while true\n   (println :foo))"))
;     (is (= (f/reformat-string "#(reify Closeable\n(close [_]\n(prn %)))")
;            "#(reify Closeable\n   (close [_]\n     (prn %)))")))

;   (testing "multiple arities"
;     (is (= (f/reformat-string "(fn\n([x]\n(foo)\n(bar)))")
;            "(fn\n  ([x]\n   (foo)\n   (bar)))")))

;   (testing "comments"
;     (is (= (f/reformat-string ";foo\n(def x 1)")
;            ";foo\n(def x 1)"))
;     (is (= (f/reformat-string "(ns foo.core)\n\n;; foo\n(defn foo [x]\n(inc x))")
;            "(ns foo.core)\n\n;; foo\n(defn foo [x]\n  (inc x))"))
;     (is (= (f/reformat-string ";; foo\n(ns foo\n(:require bar))")
;            ";; foo\n(ns foo\n  (:require bar))"))
;     (is (= (f/reformat-string "(defn foo [x]\n  ;; +1\n(inc x))")
;            "(defn foo [x]\n  ;; +1\n  (inc x))"))
;     (is (= (f/reformat-string "(let [;foo\n x (foo bar\n baz)]\n x)")
;            "(let [;foo\n      x (foo bar\n             baz)]\n  x)"))
;     (is (= (f/reformat-string "(binding [x 1] ; foo\nx)")
;            "(binding [x 1] ; foo\n  x)")))

;   (testing "metadata"
;     (is (= (f/reformat-string "(defonce ^{:doc \"foo\"}\nfoo\n:foo)")
;            "(defonce ^{:doc \"foo\"}\n  foo\n  :foo)"))
;     (is (= (f/reformat-string "(def ^:private\nfoo\n:foo)")
;            "(def ^:private\n  foo\n  :foo)"))
;     (is (= (f/reformat-string "(def ^:private foo\n:foo)")
;            "(def ^:private foo\n  :foo)")))

;   (testing "fuzzy matches"
;     (is (= (f/reformat-string "(with-foo x\ny\nz)")
;            "(with-foo x\n  y\n  z)"))
;     (is (= (f/reformat-string "(defelem foo [x]\n[:foo x])")
;            "(defelem foo [x]\n  [:foo x])")))

;   (testing "comment before ending bracket"
;     (is (= (f/reformat-string "(foo a ; b\nc ; d\n)")
;            "(foo a ; b\n     c ; d\n     )"))
;     (is (= (f/reformat-string "(do\na ; b\nc ; d\n)")
;            "(do\n  a ; b\n  c ; d\n  )"))
;     (is (= (f/reformat-string "(let [x [1 2 ;; test1\n2 3 ;; test2\n]])")
;            "(let [x [1 2 ;; test1\n         2 3 ;; test2\n         ]])")))

;   (testing "indented comments with blank lines"
;     (is (= (f/reformat-string "(;a\n\n ;b\n )")
;            "(;a\n\n ;b\n )")))

;   (testing "indentated forms in letfn block"
;     (is (= (f/reformat-string "(letfn [(f [x]\nx)]\n(let [x (f 1)]\n(str x 2\n3 4)))")
;            (str "(letfn [(f [x]\n          x)]\n"
;                 "  (let [x (f 1)]\n    (str x 2\n         3 4)))")))))

; (deftest test-surrounding-whitespace
;   (testing "surrounding spaces"
;     (is (= (f/reformat-string "( foo bar )")
;            "(foo bar)"))
;     (is (= (f/reformat-string "[ 1 2 3 ]")
;            "[1 2 3]"))
;     (is (= (f/reformat-string "{  :x 1, :y 2 }")
;            "{:x 1, :y 2}")))

;   (testing "surrounding newlines"
;     (is (= (f/reformat-string "(\n  foo\n)")
;            "(foo)"))
;     (is (= (f/reformat-string "(  \nfoo\n)")
;            "(foo)"))
;     (is (= (f/reformat-string "(foo  \n)")
;            "(foo)"))
;     (is (= (f/reformat-string "(foo\n  )")
;            "(foo)"))
;     (is (= (f/reformat-string "[\n1 2 3\n]")
;            "[1 2 3]"))
;     (is (= (f/reformat-string "{\n:foo \"bar\"\n}")
;            "{:foo \"bar\"}"))
;     (is (= (f/reformat-string "( let [x 3\ny 4]\n(+ (* x x\n)(* y y)\n))")
;            "(let [x 3\n      y 4]\n  (+ (* x x) (* y y)))"))))

; (deftest test-missing-whitespace
;   (is (= (f/reformat-string "(foo(bar baz)qux)")
;          "(foo (bar baz) qux)"))
;   (is (= (f/reformat-string "(foo)bar(baz)")
;          "(foo) bar (baz)"))
;   (is (= (f/reformat-string "(foo[bar]#{baz}{quz bang})")
;          "(foo [bar] #{baz} {quz bang})")))

; (deftest test-consecutive-blank-lines
;   (is (= (f/reformat-string "(foo)\n\n(bar)")
;          "(foo)\n\n(bar)"))
;   (is (= (f/reformat-string "(foo)\n\n\n(bar)")
;          "(foo)\n\n(bar)"))
;   (is (= (f/reformat-string "(foo)\n \n \n(bar)")
;          "(foo)\n\n(bar)"))
;   (is (= (f/reformat-string "(foo)\n\n\n\n\n(bar)")
;          "(foo)\n\n(bar)"))
;   (is (= (f/reformat-string "(foo)\n\n;bar\n\n(baz)")
;          "(foo)\n\n;bar\n\n(baz)"))
;   (is (= (f/reformat-string "(foo)\n;bar\n;baz\n;qux\n(bang)")
;          "(foo)\n;bar\n;baz\n;qux\n(bang)")))

; (deftest test-options
;   (is (= (f/reformat-string "(foo)\n\n\n(bar)" {:remove-consecutive-blank-lines? false})
;          "(foo)\n\n\n(bar)"))
;   (is (= (f/reformat-string "(  foo  )" {:remove-surrounding-whitespace? false})
;          "(  foo  )"))
;   (is (= (f/reformat-string "(foo(bar))" {:insert-missing-whitespace? false})
;          "(foo(bar))"))
;   (is (= (f/reformat-string "(foo\nbar)" {:indents '{foo [[:block 0]]}})
;          "(foo\n  bar)"))
;   (is (= (f/reformat-string "(do\nfoo\nbar)" {:indents {}})
;          "(do\n foo\n bar)"))
;   (is (= (f/reformat-string "(do\nfoo\nbar)" {:indentation? false})
;          "(do\nfoo\nbar)")))

 (deftest test-parsing
   (is (= (f/reformat-string ";foo") ";foo"))
   (is (= (f/reformat-string "::foo") "::foo"))
   (is (= (f/reformat-string "::foo/bar") "::foo/bar"))
   (is (= (f/reformat-string "foo:bar") "foo:bar"))
   (is (= (f/reformat-string "#_(foo\nbar)") "#_(foo\n   bar)")) ; failing
   (is (= (f/reformat-string "(juxt +' -')") "(juxt +' -')")))
