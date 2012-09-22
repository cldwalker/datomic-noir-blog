(defproject noir-blog "0.1.0"
            :description "A fully functional blog that serves an example of a noir project."
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [clj-time "0.3.7"]
                           [noir "1.3.0-beta10"]
                           [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                           [com.datomic/datomic-free "0.8.3524"]
                           [simpledb "0.1.4"]]
            :main noir-blog.server)

