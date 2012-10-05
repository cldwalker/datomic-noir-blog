(ns noir-blog.server
  (:require [noir.server :as server]
            [datomic-simple.core :as db]
            noir-blog.models
            [noir-blog.models :as models]))

(server/load-views "src/noir_blog/views/")

(server/add-middleware db/wrap-datomic noir-blog.models/uri)

(defn -main [& m]
  (let [mode (or (first m) :dev)
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (models/initialize)
    (server/start port {:mode (keyword mode)
                        :ns 'noir-blog})))

