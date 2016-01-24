(ns cljsfmt.core
  (:require [clojure.zip :as zip]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.base :as zb]
            [cljsfmt.indents.clojurescript :as ci]
            [cljsfmt.indents.fuzzy :as fi]))

(defn- edit-all [zloc p? f]
  (loop [zloc (if (p? zloc) (f zloc) zloc)]
    (if-let [zloc (z/find-next zloc zip/next p?)]
      (recur (f zloc))
      zloc)))

(defn- transform [form zf & args]
  (z/root (apply zf (zb/edn form) args)))

(defn- surrounding? [zloc p?]
  (and (p? zloc) (or (nil? (zip/left zloc))
                     (nil? (z/skip zip/right p? zloc)))))

(defn- top? [zloc]
  (and zloc (not= (z/node zloc) (z/root zloc))))

(defn- surrounding-whitespace? [zloc]
  (and (top? (z/up zloc))
       (surrounding? zloc z/whitespace?)))

(defn remove-surrounding-whitespace [form]
  (transform form edit-all surrounding-whitespace? zip/remove))

(defn- element? [zloc]
  (if zloc (not (z/whitespace-or-comment? zloc))))

(defn- missing-whitespace? [zloc]
  (and (element? zloc) (element? (zip/right zloc))))

(defn insert-missing-whitespace [form]
  (transform form edit-all missing-whitespace? z/append-space))

(defn- whitespace? [zloc]
  (= (z/tag zloc) :whitespace))

(defn- comment? [zloc]
  (some-> zloc z/node n/comment?))

(defn- line-break? [zloc]
  (or (z/linebreak? zloc) (comment? zloc)))

(defn- skip-whitespace [zloc]
  (z/skip zip/next whitespace? zloc))

(defn- count-newlines [zloc]
  (loop [zloc zloc 
         newlines 0]
    (if (z/linebreak? zloc)
      (recur (-> zloc zip/next skip-whitespace)
             (-> zloc z/string count (+ newlines)))
      newlines)))

(defn- consecutive-blank-line? [zloc]
  (> (count-newlines zloc) 2))

(defn- remove-whitespace-and-newlines [zloc]
  (if (z/whitespace? zloc)
    (recur (zip/remove zloc))
    zloc))

(defn- replace-consecutive-blank-lines [zloc]
  (-> zloc (zip/replace (n/newlines 2)) zip/next remove-whitespace-and-newlines))

(defn remove-consecutive-blank-lines [form]
  (transform form edit-all consecutive-blank-line? replace-consecutive-blank-lines))

(defn- indentation? [zloc]
  (and (line-break? (zip/prev zloc)) (whitespace? zloc)))

(defn- comment-next? [zloc]
  (-> zloc zip/next skip-whitespace comment?))

(defn- line-break-next? [zloc]
  (-> zloc zip/next skip-whitespace line-break?))

(defn- should-indent? [zloc]
  (and (line-break? zloc) (not (line-break-next? zloc))))

(defn- should-unindent? [zloc]
  (and (indentation? zloc) (not (comment-next? zloc))))

(defn unindent [form]
  (transform form edit-all should-unindent? zip/remove))

(def ^:private start-element
  {:meta "^", :meta* "#^", :vector "[",       :map "{"
   :list "(", :eval "#=",  :uneval "#_",      :fn "#("
   :set "#{", :deref "@",  :reader-macro "#", :unquote "~"
   :var "#'", :quote "'",  :syntax-quote "`", :unquote-splicing "~@"})

(defn- prior-string [zloc]
  (if-let [p (z/left* zloc)]
    (str (prior-string p) (n/string (z/node p)))
    (if-let [p (z/up* zloc)]
      (str (prior-string p) (start-element (n/tag (z/node p))))
      "")))

(defn- last-line-in-string [^String s]
  (subs s (inc (.lastIndexOf s "\n"))))

(defn- margin [zloc]
  (-> zloc prior-string last-line-in-string count))

(defn- whitespace [width]
  (n/whitespace-node (apply str (repeat width " "))))

(defn- coll-indent [zloc]
  (-> zloc zip/leftmost margin))

(defn- index-of [zloc]
  (->> (iterate z/left zloc)
       (take-while identity)
       (count)
       (dec)))

(defn- list-indent [zloc]
  (if (> (index-of zloc) 1)
    (-> zloc zip/leftmost z/right margin)
    (coll-indent zloc)))

(def indent-size 2)

(defn- indent-width [zloc]
  (case (z/tag zloc)
    :list indent-size
    :fn   (inc indent-size)))

(defn- remove-namespace [x]
  (if (symbol? x) (symbol (name x)) x))

(defn- indent-matches? [key sym]
  (condp instance? key
    ; Symbol  (= key sym)
    js/RegExp (re-find key (str sym))))

(defn- token? [zloc]
  (= (z/tag zloc) :token))

(defn- token-value [zloc]
  (if (token? zloc) (z/value zloc)))

(defn- form-symbol [zloc]
  (-> zloc z/leftmost token-value remove-namespace))

(defn- index-matches-top-argument? [zloc depth idx]
  (and (> depth 0)
       (= idx (index-of (nth (iterate z/up zloc) (dec depth))))))

(defn- inner-indent [zloc key depth idx]
  (let [top (nth (iterate z/up zloc) depth)]
    (if (and (indent-matches? key (form-symbol top))
             (or (nil? idx) (index-matches-top-argument? zloc depth idx)))
      (let [zup (z/up zloc)]
        (+ (margin zup) (indent-width zup))))))

(defn- nth-form [zloc n]
  (reduce (fn [z f] (if z (f z)))
          (z/leftmost zloc)
          (repeat n z/right)))

(defn- first-form-in-line? [zloc]
  (if-let [zloc (zip/left zloc)]
    (if (whitespace? zloc)
      (recur zloc)
      (or (z/linebreak? zloc) (comment? zloc)))
    true))

(defn- block-indent [zloc key idx]
  (if (indent-matches? key (form-symbol zloc))
    (if (and (some-> zloc (nth-form (inc idx)) first-form-in-line?)
             (> (index-of zloc) idx))
      (inner-indent zloc key 0 nil)
      (list-indent zloc))))

(def default-indents
  (merge fi/fuzzy-indents
         ci/cljs-indents))

(defmulti ^:private indenter-fn
  (fn [sym [type & args]] type))

(defmethod indenter-fn :inner [sym [_ depth idx]]
  (fn [zloc] (inner-indent zloc sym depth idx)))

(defmethod indenter-fn :block [sym [_ idx]]
  (fn [zloc] (block-indent zloc sym idx)))

(defn- make-indenter [[key opts]]
  (apply some-fn (map (partial indenter-fn key) opts)))

(defn- indent-order [[key _]]
  (condp instance? key
    ;Symbol  (str 0 key)
    js/RegExp (str 1 key)))

(defn- custom-indent [zloc indents]
  (if (empty? indents)
    (list-indent zloc)
    (let [indenter (->> (sort-by indent-order indents)
                        (map make-indenter)
                        (apply some-fn))]
      (or (indenter zloc)
          (list-indent zloc)))))

(defn- indent-amount [zloc indents]
  (case (-> zloc z/up z/tag)
    (:list :fn) (custom-indent zloc indents)
    :meta       (indent-amount (z/up zloc) indents)
    (coll-indent zloc)))

(defn- indent-line [zloc indents]
  (let [width (indent-amount zloc indents)]
    (if (> width 0)
      (zip/insert-right zloc (whitespace width))
      zloc)))

(defn reindent [form indents]
  (transform form edit-all should-indent? #(indent-line % (or indents default-indents))))

(defn reformat-form [form & [{:as opts}]]
  (-> form
    (cond-> (:remove-consecutive-blank-lines? opts true)
      remove-consecutive-blank-lines)
    (cond-> (:remove-surrounding-whitespace? opts true)
      remove-surrounding-whitespace)
    (cond-> (:insert-missing-whitespace? opts true)
      insert-missing-whitespace)
    (cond-> (:indentation? opts true)
      (reindent (:indents opts default-indents)))))

(defn reformat-string [form-string & [options]]
  (-> (p/parse-string-all form-string)
      (reformat-form options)
      (n/string)))

(reformat-string "(foo bar\nbaz\nquz)")
