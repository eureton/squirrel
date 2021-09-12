(ns treeduce.core
  (:refer-clojure :exclude [map reduce seq])
  (:require [clojure.core :as core]
            [treeduce.node :as node]))

(def ^:dynamic *identity*
  "Identity element for the treeduce.core/add binary operation."
  nil)

(defn add
  "Makes tree y the last descendant of tree x. If x has no children, y becomes
   the first child of x. Is associative with treeduce.core/id-elem as identity
   element. Variadic version is shorthand for reducing over the arguments."
  ([x] x)
  ([x y]
   (cond (= *identity* x) y
         (= *identity* y) x
         (node/leaf? x) (node/update-children x (comp vec conj) y)
         :else (node/update-nth-child x (-> x node/fanout dec) add y)))
  ([x y & ys]
   (core/reduce add x (conj ys y))))

(defmulti seq-inner
  "Don't call this directly, use treeduce.core/seq instead."
  (fn [_ options] (-> options :traversal)))

(defmethod seq-inner :depth-first
  [tree _]
  (when (and tree (not= *identity* tree))
    (->> tree
         (tree-seq (complement node/leaf?) node/*children*)
         (core/map node/*data*))))

(defmethod seq-inner :breadth-first
  [tree options]
  (when (and tree (not= *identity* tree))
    (let [[data children] ((juxt node/*data* node/*children*) tree)
          children-seqs (core/map #(seq-inner % options) children)]
      (concat [data]
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
   calls."
  [f tree]
  (some-> tree
          f
          (node/update-children (comp vec #(core/map %2 %1)) #(map f %))))

(defn reduce
  "Reduces tree to a value by applying f to the data of each node in tree. Has
   the same signature as clojure.core/reduce, with the addition of traversal,
   which must be either :depth-first or :breadth-first."
  ([f init tree traversal]
   (core/reduce f init (seq tree {:traversal traversal})))
  ([f tree traversal]
   (core/reduce f (seq tree {:traversal traversal}))))

