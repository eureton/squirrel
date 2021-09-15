# squirrel

Knows his way around a tree.

## About

Library of functions for dealing with tree data structures. `map`, `filter`, `reduce` and more.

## Installation

### Leiningen

Add the following to `:dependencies` in your `project.clj`:

```
[squirrel "0.1.0"]
```

## Usage

### Nodes

Make a tree node:

``` clojure
squirrel.core=> (squirrel.node/make 1)
{:data 1}
```

Use the shorthand:

``` clojure
squirrel.core=> (require '[squirrel.node :refer [node]])
nil
squirrel.core=> (node 1)
{:data 1}
```

With aggregated data:

``` clojure
squirrel.core=> (node {:x 10 :y "zzz"})
{:data {:x 10, :y "zzz"}}
```

### Trees

Trees are nodes with children nodes attached to them:

``` clojure
squirrel.core=> (node 1 [(node 2) (node 3)])
{:data 1, :children [{:data 2} {:data 3}]}
```

Map:

``` clojure
squirrel.core=> (require '[squirrel.tree :as tree])
nil
squirrel.core=> (tree/map #(update % :data * 10)
           #_=>           (node 1 [(node 2) (node 3)]))
{:data 10, :children [{:data 20} {:data 30}]}
```

Filter:

``` clojure
squirrel.core=> (tree/filter (comp odd? :data)
           #_=>              (node 1 [(node 2) (node 3) (node 4)]))
{:data 1, :children [{:data 3}]}
```

Reduce:

``` clojure
squirrel.core=> (tree/reduce +
           #_=>              (node 1 [(node 2) (node 3)])
           #_=>              :depth-first)
6
```

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
