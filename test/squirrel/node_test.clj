(ns squirrel.node-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [squirrel.node :refer :all]))

(deftest leaf?-test
  (testing "nil"
    (is (true? (leaf? nil))))
  
  (testing ":children is nil"
    (is (true? (leaf? (make :_ nil)))))
  
  (testing ":children is empty"
    (is (true? (leaf? (make :_ []))))))

(deftest not-leaf?-test
  (testing "nil"
    (is (false? (not-leaf? nil))))

  (testing ":children is nil"
    (is (false? (not-leaf? (make :_ nil)))))

  (testing ":children is empty"
    (is (false? (not-leaf? (make :_ []))))))

(deftest make-test
  (testing "data"
    (is (= (make :abc)
           {:data :abc
            :children []})))

  (testing "children"
    (is (= (make :a [(make :b) (make :c)])
           {:data :a
            :children [{:data :b
                        :children []}
                       {:data :c
                        :children []}]})))

  (testing "children is nil"
    (is (= (make :abc nil)
           {:data :abc
            :children nil})))

  (testing "children is empty collection"
    (is (= (make :abc [])
           {:data :abc
            :children []})))

  (testing "override defaults"
    (testing "hiccup notation"
      (is (= (binding [*seed* []
                       *data-readf* first
                       *data-writef* #(assoc %2 0 %1)
                       *children-readf* second
                       *children-writef* #(assoc %2 1 %1)]
               (make :a [(make :b) (make :c)]))
             [:a [[:b []]
                  [:c []]]])))

    (testing "meta data"
      (is (= (binding [*data-readf* :payload
                       *data-writef* #(assoc %2 :payload %1)
                       *children-writef* #(-> %2
                                              (assoc-in [:meta :num] (count %1))
                                              (assoc :children %1))]
               (make :a [(make :b) (make :c)]))
             {:payload :a
              :meta {:num 2}
              :children [{:payload :b
                          :meta {:num 0}
                          :children []}
                         {:payload :c
                          :meta {:num 0}
                          :children []}]})))))

(deftest add-test
  (testing "to leaf"
    (testing "one"
      (is (= (add (make 1) (make 2))
             (make 1 [(make 2)]))))

    (testing "many"
      (is (= (add (make 1) (make 2) (make 3) (make 4))
             (make 1 [(make 2) (make 3) (make 4)])))))

  (testing "to non-leaf"
    (testing "one"
      (is (= (add (make :a [(make :b)]) (make :c))
             (make :a [(make :b) (make :c)]))))

    (testing "many"
      (is (= (add (make :a [(make :b) (make :c)])
                  (make :d))
             (make :a [(make :b)
                       (make :c)
                       (make :d)]))))))

