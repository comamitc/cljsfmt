(ns cljsfmt.util
  (:require [rewrite-clj.node.protocols :as np]))

(defn log
  "Log a Clojure thing."
  [thing]
  (js/console.log (pr-str thing)))

(defn js-log
  "Log a JavaScript thing."
  [thing]
  (js/console.log thing))

(defmacro import-def
  "import a single fn or var
   (import-def a b) => (def b a/b)
  "
  [from-ns def-name]
  (let [from-sym# (symbol (str from-ns) (str def-name))]
    `(def ~def-name ~from-sym#)))

(defmacro import-vars
  "import multiple defs from multiple namespaces
   works for vars and fns. not macros.
   (same syntax as potemkin.namespaces/import-vars)
   (import-vars
     [m.n.ns1 a b]
     [x.y.ns2 d e f]) =>
   (def a m.n.ns1/a)
   (def b m.n.ns1/b)
    ...
   (def d m.n.ns2/d)
    ... etc
  "
  [& imports]
  (let [expanded-imports (for [[from-ns & defs] imports
                               d defs]
                           `(import-def ~from-ns ~d))]
  `(do ~@expanded-imports)))

;; TODO: add these functions to rewrite-cljs
(defn node-value
  [node]
  (if (np/inner? node)
    (some-> (np/children node)
            (first)
            ((juxt np/tag np/sexpr)))
    (np/sexpr node)))

(defn left
  "Returns the loc of the left sibling of the node at this loc, or nil"
  [loc]
  (let [{:keys [node parent left right]} loc]
    (when (and parent (seq left))
      (let [[lnode lpos] (peek left)]
        (assoc loc
               :node lnode
               :position lpos
               :left (pop left)
               :right (cons node right))))))

(defn ^:no-doc make-node
  "Returns a new branch node, given an existing node and new
  children. The loc is only used to supply the constructor."
  [loc node children]
  (np/replace-children node children))

(defn up
  "Returns the loc of the parent of the node at this loc, or nil if at
  the top"
  [loc]
  (let [{:keys [node parent left right changed?]} loc]
    (when parent
      (if changed?
        (assoc parent
               :changed? true
               :node (make-node loc
                                (:node parent)
                                (concat (map first left) (cons node right))))
        parent))))
