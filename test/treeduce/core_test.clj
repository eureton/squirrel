(ns treeduce.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [treeduce.core :as treeduce]))

(def left (-> (treeduce/node "2-1")
              (treeduce/add (treeduce/node "3-1-1"))
              (treeduce/add (treeduce/node "3-1-2"))))
(def right (-> (treeduce/node "2-2")
               (treeduce/add (treeduce/node "3-2-1"))
               (treeduce/add (treeduce/node "3-2-2"))))
(def tree (-> (treeduce/node "1")
              (treeduce/add left)
              (treeduce/add right)))

(deftest leaf?-test
  (testing "nil => true"
    (true? (treeduce/leaf? nil)))
  
  (testing ":children is nil"
    (true? (treeduce/leaf? {:children nil})))
  
  (testing ":children is empty"
    (true? (treeduce/leaf? {:children []}))))

(deftest seq-test
  (testing "nil => nil"
    (nil? (treeduce/seq nil)))
  
  (testing "empty => nil"
    (nil? (treeduce/seq treeduce/zero)))
  
  (testing "depth-first"
    (is (= (treeduce/seq tree {:traversal :depth-first})
           ["1" "2-1" "3-1-1" "3-1-2" "2-2" "3-2-1" "3-2-2"])))

  (testing "breadth-first"
    (is (= (treeduce/seq tree {:traversal :breadth-first})
           ["1" "2-1" "2-2" "3-1-1" "3-1-2" "3-2-1" "3-2-2"])))

  (testing "default is depth-first"
    (is (= (treeduce/seq tree)
           (treeduce/seq tree {:traversal :depth-first})))))

(deftest map-test
  (testing "pun nil"
    (nil? (treeduce/map even? nil)))
  
  (testing "standard"
    (let [tree (-> (treeduce/node 2)
                   (treeduce/add (treeduce/node 3))
                   (treeduce/add (treeduce/node 4)))]

      (is (= (treeduce/map #(update % :data * 2) tree)
             {:data 4
              :children [{:data 6}
                         {:data 8}]})))))

(deftest reduce-test
  (defn rf
    [agg x]
    (if-not (string/starts-with? x "3-2")
      (string/join ", " [agg x])
      agg))

  (testing "standard"
    (let [tree (-> (treeduce/node 2)
                   (treeduce/add (treeduce/node 3))
                   (treeduce/add (treeduce/node 4)))]
      (is (= (treeduce/reduce + tree :depth-first)
             9))))
  
  (testing "depth-first"
    (is (= (treeduce/reduce rf tree :depth-first)
           "1, 2-1, 3-1-1, 3-1-2, 2-2")))

  (testing "breadth-first"
    (is (= (treeduce/reduce rf tree :breadth-first)
           "1, 2-1, 2-2, 3-1-1, 3-1-2")))

  (testing "init value"
    (is (= (treeduce/reduce rf "@" tree :depth-first)
           (str "@, " (treeduce/reduce rf tree :depth-first))))))

