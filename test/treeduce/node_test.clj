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

