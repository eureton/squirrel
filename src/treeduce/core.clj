(ns treeduce.core
  (:refer-clojure :exclude [map reduce seq])
  (:require [clojure.core :as core]))

(defn node
  ""
  [data]
  {:data data})

(def zero
  ""
  nil)

(defn add
  ""
  [acc x]
  (if (= x zero)
    acc
    (update acc :children (comp vec conj) x)))

(def leaf?
  ""
  (comp empty? :children))

(defmulti seq
  ""
  (fn [_ & options] (-> options first :traversal)))

(defmethod seq :depth-first
  [tree & options]
  (when tree
    (->> tree
         (tree-seq (complement leaf?) :children)
         (core/map :data))))

(defmethod seq :breadth-first
  [tree & options]
  (when tree
    (let [{:keys [data children]} tree
          children-seqs (core/map #(apply seq % options) children)]
      (concat [data]
              (core/map first children-seqs)
              (mapcat rest children-seqs)))))

(defmethod seq nil
  [tree & _]
  (seq tree {:traversal :depth-first}))

(defn sweep
  ""
  [tree]
  (cond-> tree
    (empty? (:children tree)) (dissoc :children)))

(defn map
  ""
  [f tree]
  (let [{:keys [children]} tree]
    (some-> tree
            f
            (update :children (comp vec #(core/map %2 %1)) #(map f %))
            sweep)))

(defn reduce
  ([f init tree traversal]
   (core/reduce f init (seq tree {:traversal traversal})))
  ([f tree traversal]
   (core/reduce f (seq tree {:traversal traversal}))))

