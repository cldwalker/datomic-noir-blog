(ns noir-blog.models
  (:require [noir-blog.datomic :as db]
            [noir-blog.models.user :as users]
            [noir-blog.models.post :as posts]))

(defn initialize []
  (db/init)
  (users/init!)
  (posts/init!))
