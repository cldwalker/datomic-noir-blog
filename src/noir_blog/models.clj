(ns noir-blog.models
  (:require [simpledb.core :as db]
            [noir-blog.datomic :as datomic]
            [noir-blog.models.user :as users]
            [noir-blog.models.post :as posts]))

(defn initialize []
  (datomic/init)
  (users/init!)
  (posts/init!))
