(ns treeduce.node)

(def ^:dynamic *data*
  "Keyword to access data by."
  :data)

(def ^:dynamic *children*
  "Keyword to access children by."
  :children)

(defn leaf?
  "True if node has no children, false otherwise."
  [node]
  (empty? (*children* node)))

(defn add
  "Makes node y the last child of node x. If x has no children, y becomes the
   first child of x."
  [x & ys]
  (update x *children* (comp vec concat) ys))

(defn fanout
  "Number of children in the node."
  [node]
  (-> node *children* count))

(defn sweep
  "Keeps the node hash free of empty or nil children collections."
  [node]
  (cond-> node
    (leaf? node) (dissoc *children*)))

(defn make
  "Node with the given data and children."
  ([data children]
   (sweep {*data* data
           *children* children}))
  ([data]
   (make data nil)))

(defn update-children
  "Shorthand for sweeping after updating node with f on *children*."
  [node f & args]
  (sweep (apply update node *children* f args)))

(defn update-nth-child
  "Applies function f to arguments, preceded by the n-th child of node."
  [node n f & args]
  (apply update-in node [*children* n] f args))

