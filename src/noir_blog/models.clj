(ns noir-blog.models
  (:require [noir-blog.datomic :as db]
            [datomic.api :as d]
            [noir-blog.models.user :as users]
            [noir-blog.models.post :as posts]))

(defn initialize []
  (db/init)
  (binding [db/*connection* (d/connect db/uri)]
    (users/init!)
    (posts/init!)))
