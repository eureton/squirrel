(ns squirrel.tree-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [squirrel.tree :refer [*identity* add sweep]]
            [squirrel.tree :as tree]
            [squirrel.node :as node :refer [node]]))

(deftest add-test
  (testing "monoid"
    (testing "identity element"
      (testing "left"
        (are [x] (= x (add *identity* x))
             *identity*
             (node "abc")))

      (testing "right"
        (are [x] (= x (add x *identity*))
             *identity*
             (node "abc")))

      (testing "override"
        (let [x (node "abc")]

          (testing "accepts binding"
            (are [l r] (= x (binding [*identity* {}]
                              (add l r)))
                 {} x
                 x {}))

          (testing "disregards default"
            (are [l r] (not= x (binding [*identity* {}]
                                 (add l r)))
                 nil x
                 x nil)))))

    (testing "associativity"
      (let [x (node :x)
            y (node :y)
            z (node :z)]
        (is (= (add (add x y) z)
               (add x (add y z)))))))

  (testing "one"
    (is (= (add (node "parent")
                (node "child"))
           (node "parent" [(node "child")]))))

  (testing "many"
    (is (= (add (node "A")
                (node "B")
                *identity*
                (node "C")
                (node "D"))
           (node "A"
                 [(node "B"
                        [(node "C"
                               [(node "D")])])]))))

  (testing "as reducer"
    (testing "without init"
      (is (= (reduce add [(node 1) (node 2) (node 3)])
             (node 1
                   [(node 2
                          [(node 3)])]))))

    (testing "with init"
      (is (= (reduce add
                     (node 0)
                     [(node 1) (node 2) (node 3)])
             (node 0
                   [(node 1
                          [(node 2
                                 [(node 3)])])]))))

    (testing "nil collection"
      (is (= *identity* (reduce add nil))))

    (testing "empty collection"
      (is (= *identity* (reduce add []))))))

(deftest seq-test
  (testing "nil"
    (nil? (tree/seq nil)))
  
  (testing "empty"
    (nil? (tree/seq *identity*)))
  
  (testing "traversal"
    (let [tree (node 1 [(node 2 [(node 3) (node 4)])
                        (node 5 [(node 6) (node 7)])])]

      (testing "depth-first"
        (is (= (tree/seq tree {:traversal :depth-first})
               [1 2 3 4 5 6 7])))

      (testing "breadth-first"
        (is (= (tree/seq tree {:traversal :breadth-first})
               [1 2 5 3 4 6 7])))

      (testing "default"
        (is (= (tree/seq tree)
               (tree/seq tree {:traversal :depth-first}))))))

  (testing "override defaults"
    (is (= (binding [node/*leaf?* (some-fn (comp empty? :children)
                                           (comp odd? :data))]
             (tree/seq (node 0 [(node 1
                                      [(node 2)])
                                (node 3)])))
           [0 1 3]))))

(deftest map-test
  (testing "nil"
    (is (= *identity* (tree/map even? nil))))

  (testing "identity"
    (is (= *identity* (tree/map even? *identity*))))
  
  (testing "preserves structure"
    (is (= (tree/map #(update % :data * 10)
                     (node 1 [(node 2) (node 3)]))
           (node 10 [(node 20) (node 30)]))))

  (testing "alters structure"
    (is (= (tree/map (fn [x]
                       (update x :children #(filter (comp odd? :data) %)))
                     (node 1 [(node 2 [(node 20)
                                       (node 21)
                                       (node 22)])
                              (node 3 [(node 30)
                                       (node 31)
                                       (node 32)])
                              (node 4 [(node 40)
                                       (node 41)
                                       (node 42)])]))
                
           (node 1
                 [(node 3
                        [(node 31)])]))))

  (testing "override defaults"
    (binding [node/*data-readf* :d
              node/*data-writef* #(assoc %2 :d %1)]
      (is (= (tree/map #(update % :d * 100)
                       (node 1 [(node 2) (node 3)]))
             (node 100 [(node 200) (node 300)])))))

  (testing "laziness"
    (is (not (realized? (->> (node 1 [(node 2) (node 3)])
                             (tree/map #(update % :data inc))
                             node/*children-readf*)))))

  (testing "postprocessing"
    (is (= (tree/map #(update % :data inc)
                     #(->> %
                           :children
                           (map :data)
                           clojure.string/join
                           (update % :data str))
                (node 1 [(node 10 [(node 100)
                                   (node 200)])
                         (node 20)]))
           (node "21110120121" [(node "11101201" [(node "101")
                                                  (node "201")])
                                (node "21")])))))

(deftest filter-test
  (testing "nil"
    (is (= *identity* (tree/filter :x nil))))

  (testing "identity"
    (is (= *identity* (tree/filter :x *identity*))))

  (testing "root qualifies"
    (is (= (tree/filter (comp odd? :data)
                        (node 1 [(node 2 [(node 3)
                                          (node 4)])
                                 (node 5 [(node 6)
                                          (node 7)])]))
           (node 1
                 [(node 5
                        [(node 7)])]))))

  (testing "root doesn't qualify"
    (is (= (tree/filter (comp even? :data)
                        (node 1 [(node 2)]))
           *identity*)))

  (testing "laziness"
    (is (not (realized? (->> (node 1 [(node 2) (node 3)])
                             (tree/filter (comp odd? :data))
                             :children))))))

(deftest reduce-test
  (testing "standard"
    (is (= (tree/reduce +
                        (node 2 [(node 3) (node 4)])
                        :depth-first)
           9)))

  (testing "traversal"
    (let [tree (node 1 [(node 2 [(node 3)
                                 (node 4)])
                        (node 5 [(node 6)
                                 (node 7)])])]

      (testing "depth-first"
        (is (= (tree/reduce str tree :depth-first)
               "1234567")))

      (testing "breadth-first"
        (is (= (tree/reduce str tree :breadth-first)
               "1253467")))

      (testing "init value"
        (is (= (tree/reduce str "@" tree :depth-first)
               (str "@" (tree/reduce str tree :depth-first))))))))

