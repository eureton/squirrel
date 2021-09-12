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

