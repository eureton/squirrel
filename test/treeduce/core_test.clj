(ns treeduce.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [treeduce.core :refer [*identity* add]]
            [treeduce.core :as tree]
            [treeduce.node :as node]))

(def left {:data "2-1"
           :children [{:data "3-1-1"}
                      {:data "3-1-2"}]})
(def right {:data "2-2"
            :children [{:data "3-2-1"}
                       {:data "3-2-2"}]})
(def tree {:data "1"
            :children [left right]})

(deftest add-test
  (testing "monoid"
    (testing "identity element"
      (testing "left"
        (are [x] (= x (add *identity* x))
             *identity*
             {:data "abc"}))

      (testing "right"
        (are [x] (= x (add x *identity*))
             *identity*
             {:data "abc"}))

      (testing "override"
        (def x {:data "abc"})

        (testing "accepts binding"
          (are [l r] (= x (binding [*identity* {}]
                            (add l r)))
               {} x
               x {}))

        (testing "disregards default"
          (are [l r] (not= x (binding [*identity* {}]
                               (add l r)))
               nil x
               x nil))))

    (testing "associativity"
      (def x {:data :x})
      (def y {:data :y})
      (def z {:data :z})

      (is (= (add (add x y) z)
             (add x (add y z))))))

  (testing "one"
    (is (= (add {:data "parent"}
                {:data "child"})
           {:data "parent"
            :children [{:data "child"}]})))

  (testing "many"
    (is (= (add {:data "A"}
                {:data "B"}
                *identity*
                {:data "C"}
                {:data "D"})
           {:data "A"
            :children [{:data "B"
                        :children [{:data "C"
                                    :children [{:data "D"}]}]}]})))

  (testing "as reducer"
    (testing "without init"
      (is (= (reduce add [{:data 0} {:data 1} {:data 2}])
             {:data 0
              :children [{:data 1
                          :children [{:data 2}]}]})))

    (testing "with init"
      (is (= (reduce add
                     {:data -1}
                     [{:data 0} {:data 1} {:data 2}])
             {:data -1
              :children [{:data 0
                          :children [{:data 1
                                      :children [{:data 2}]}]}]}))))
  (testing "override defaults"
    (is (= (binding [node/*children* :components]
             (reduce add
                     [{:data 0} {:data 1} {:data 2}]))
           {:data 0
            :components [{:data 1
                          :components [{:data 2}]}]}))))

(deftest seq-test
  (testing "nil"
    (nil? (tree/seq nil)))
  
  (testing "empty"
    (nil? (tree/seq *identity*)))
  
  (testing "traversal"
    (testing "depth-first"
      (is (= (tree/seq tree {:traversal :depth-first})
             ["1" "2-1" "3-1-1" "3-1-2" "2-2" "3-2-1" "3-2-2"])))

    (testing "breadth-first"
      (is (= (tree/seq tree {:traversal :breadth-first})
             ["1" "2-1" "2-2" "3-1-1" "3-1-2" "3-2-1" "3-2-2"])))

    (testing "default"
      (is (= (tree/seq tree)
             (tree/seq tree {:traversal :depth-first})))))

  (testing "override defaults"
    (testing "*node/data*"
      (is (= (binding [node/*data* :payload]
               (tree/seq {:payload 0
                          :children [{:payload 1
                                      :children [{:payload 2}]}
                                     {:payload 3}]}))
             [0 1 2 3])))

    (testing "*node/leaf?*"
      (is (= (binding [node/*leaf?* (some-fn (comp empty? :children)
                                             (comp odd? :data))]
               (tree/seq {:data 0
                          :children [{:data 1
                                      :children [{:data 2}]}
                                     {:data 3}]}))
             [0 1 3])))))

(deftest map-test
  (testing "pun nil"
    (nil? (tree/map even? nil)))
  
  (testing "preserves structure"
    (is (= (tree/map #(update % :data * 10)
                     {:data 1
                      :children [{:data 2} {:data 3}]})
           {:data 10
            :children [{:data 20}
                       {:data 30}]})))

  (testing "alters structure"
    (is (= (tree/map (fn [x]
                       (update x :children #(filter (comp odd? :data) %)))
                     {:data 1
                      :children [{:data 2 :children [{:data 20}
                                                     {:data 21}
                                                     {:data 22}]}
                                 {:data 3 :children [{:data 30}
                                                     {:data 31}
                                                     {:data 32}]}
                                 {:data 4 :children [{:data 40}
                                                     {:data 41}
                                                     {:data 42}]}]})
           {:data 1
            :children [{:data 3
                        :children [{:data 31}]}]})))

  (testing "override defaults"
    (is (= (binding [node/*data* :d
                     node/*children* :c]
             (tree/map #(update % :d * 100)
                       {:d 1 :c [{:d 2}
                                 {:d 3}]}))
           {:d 100 :c [{:d 200}
                       {:d 300}]}))))

(deftest reduce-test
  (defn rf
    [acc x]
    (cond-> acc
      (not (string/starts-with? x "3-2")) (str ", " x)))

  (testing "standard"
    (is (= (tree/reduce +
                        {:data 2 :children [{:data 3}
                                            {:data 4}]}
                        :depth-first)
           9)))
  
  (testing "depth-first"
    (is (= (tree/reduce rf tree :depth-first)
           "1, 2-1, 3-1-1, 3-1-2, 2-2")))

  (testing "breadth-first"
    (is (= (tree/reduce rf tree :breadth-first)
           "1, 2-1, 2-2, 3-1-1, 3-1-2")))

  (testing "init value"
    (is (= (tree/reduce rf "@" tree :depth-first)
           (str "@, " (tree/reduce rf tree :depth-first))))))

