;  (:import [clojure.lang Symbol]
;           [java.util.regex Pattern])

(ns cljsfmt.core
  (:require [clojure.zip :as zip]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(reformat-form 
  [form & [{:as opts}]]
  form)

(defn reformat-string [form-string & [options]]
  (-> (p/parse-string-all form-string)
      (reformat-form options)
      (n/string)))
