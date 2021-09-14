(ns squirrel.core
  (:refer-clojure :exclude [map filter remove reduce seq])
  (:require [clojure.core :as core]
            [squirrel.node :as node]))

(def ^:dynamic *identity*
  "Identity element for the squirrel.core/add binary operation."
  nil)

(defn add
  "Makes tree y the last descendant of tree x. If x has no children, y becomes
   the first child of x. Is associative with *identity* as identity element.
   Variadic version is shorthand for reducing over the arguments."
  ([] *identity*)
  ([x] x)
  ([x y]
   (cond (= *identity* x) y
         (= *identity* y) x
         (node/*leaf?* x) (node/update-children x (comp vec conj) y)
         :else (node/update-nth-child x (-> x node/fanout dec) add y)))
  ([x y & ys]
   (core/reduce add x (conj ys y))))

(defn weighty?
  "True if tree is neither nil nor *identity*, false otherwise."
  [tree]
  (and tree
       (not= *identity* tree)))

(defmulti seq-inner
  "Don't call this directly, use squirrel.core/seq instead."
  (fn [_ {:keys [traversal]}] traversal))

(defmethod seq-inner :depth-first
  [tree _]
  (when (weighty? tree)
    (->> tree
         (tree-seq (complement node/*leaf?*) node/*children-readf*)
         (core/map node/*data-readf*))))

(defmethod seq-inner :breadth-first
  [tree options]
  (when (weighty? tree)
    (let [children-seqs (core/map #(seq-inner % options)
                                  (node/*children-readf* tree))]
      (concat [(node/*data-readf* tree)]
              (core/map first children-seqs)
              (mapcat rest children-seqs)))))

(defn seq
  "Sequence representation of tree. If tree is *identity* or nil, returns nil.
   The :traversal key in the options hash may be either :depth-first or
   :breadth-first, the former being the default."
  ([tree options]
   (seq-inner tree options))
  ([tree]
   (seq tree {:traversal :depth-first})))

(defn map
  "Tree consisting of the result of applying f to each node in tree. Nodes are
   walked in breadth-first order. Any modifications f makes to the collection of
   child nodes and/or to the child nodes themselves will be observable in later
   calls. If run on nil or *identity*, returns *identity*."
  [f tree]
  (if (weighty? tree)
    (-> tree
        f
        (node/update-children #(mapv %2 %1) #(map f %)))
    *identity*))

(defn filter
  "Tree consisting of those nodes for which (pred node) returns logical true.
   Function pred must be free of side-effects. If run on nil or *identity*,
   returns *identity*."
  [pred tree]
  (if (and (weighty? tree)
           (pred tree))
    (node/update-children tree
                          (fn [nodes]
                            (->> nodes
                                 (core/map #(filter pred %))
                                 (filterv #(not= *identity* %)))))
    *identity*))

(defn remove
  "Shorthand for (squirrel.core/filter (complement pred) tree)"
  [pred tree]
  (filter (complement pred) tree))

(defn reduce
  "Reduces tree to a value by applying f to the data of each node in tree. Has
   the same signature as clojure.core/reduce, with the addition of traversal,
   which must be either :depth-first or :breadth-first."
  ([f init tree traversal]
   (core/reduce f init (seq tree {:traversal traversal})))
  ([f tree traversal]
   (core/reduce f (seq tree {:traversal traversal}))))

