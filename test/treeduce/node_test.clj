(ns treeduce.node-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [treeduce.node :refer :all]))

(deftest leaf?-test
  (testing "nil"
    (true? (leaf? nil)))
  
  (testing ":children is nil"
    (true? (leaf? {:children nil})))
  
  (testing ":children is empty"
    (true? (leaf? {:children []}))))

(deftest make-test
  (testing "data"
    (is (= {:data :abc} (make :abc))))

  (testing "children"
    (is (= (make :a [(make :b) (make :c)])
           {:data :a
            :children [{:data :b}
                       {:data :c}]})))

  (testing "children is nil"
    (is (= {:data :abc} (make :abc nil))))

  (testing "children is empty collection"
    (is (= {:data :abc} (make :abc []))))

  (testing "override defaults"
    (is (= (binding [*data* :d
                     *children* :c]
             (make 1 [(make 2) (make 3)]))
           {:d 1 :c [{:d 2}
                     {:d 3}]}))))

(deftest add-test
  (testing "to leaf node"
    (testing "one"
      (is (= (add {:data 1} {:data 2})
             {:data 1
              :children [{:data 2}]})))

    (testing "many"
      (is (= (add {:data 1} {:data 2} {:data 3} {:data 4})
             {:data 1
              :children [{:data 2} {:data 3} {:data 4}]}))))

  (testing "to branch node"
    (testing "one"
      (is (= (add {:data :x :children [{:data :y}]} {:data :z})
             {:data :x
              :children [{:data :y} {:data :z}]})))

    (testing "many"
      (is (= (add {:data :x :children [{:data :y} {:data :z}]} {:data :q})
             {:data :x
              :children [{:data :y} {:data :z} {:data :q}]})))))

