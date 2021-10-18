(ns squirrel.node)

(def ^:dynamic *seed*
  "Data structure to populate node with."
  {})

(def ^:dynamic *data-readf*
  "Function to read data by."
  :data)

(defn ^:dynamic *data-writef*
  "Function to write data by."
  [data node]
  (assoc node :data data))

(def ^:dynamic *children-readf*
  "Function to read children by."
  :children)

(defn leaf?
  "True if node has no children, false otherwise."
  [node]
  (empty? (*children-readf* node)))

(def not-leaf?
  "Shorthand for (complement leaf?)"
  (complement leaf?))

(defn ^:dynamic *sweep*
  "Keeps the node hash free of empty or nil children collections."
  [node]
  (cond-> node
    (leaf? node) (dissoc :children)))

(defn ^:dynamic *children-writef*
  "Function to write children by."
  [children node]
  (assoc node :children children))

(defn add
  "Makes node y the last child of node x. If x has no children, y becomes the
   first child of x."
  [x & ys]
  (-> x
      *children-readf*
      (concat ys)
      (*children-writef* x)))

(defn fanout
  "Number of children in the node."
  [node]
  (-> node *children-readf* count))

(defn make
  "Node with the given data and children."
  ([data children]
   (->> *seed*
        (*data-writef* data)
        (*children-writef* children)))
  ([data]
   (make data [])))

(def node
  "Useful as shorthand symbol to :refer from."
  make)

(defn update-children
  "Shorthand for read / transform / write pipeline on children."
  [node f & args]
  (let [f #(apply f % args)]
    (-> node
        *children-readf*
        f
        (*children-writef* node))))

(defn update-nth-child
  "Applies f to an argument list consisting of the n-th child of node followed
   by args."
  [node n f & args]
  (let [f #(apply f % args)]
    (-> node
        *children-readf*
        (update n f)
        (*children-writef* node))))

(defn map-children
  "Shorthand for mapping f over children."
  [node f]
  (update-children node #(map f %)))

