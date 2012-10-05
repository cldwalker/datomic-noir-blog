(ns noir-blog.models
  (:require [datomic-simple.core :as db]
            [datomic.api :as d]
            [noir-blog.models.user :as users]
            [noir-blog.models.post :as posts]))

(def uri "datomic:mem://noir-blog")

(defn initialize []
  (db/init uri)
  (binding [db/*connection* (d/connect uri)]
    (users/init!)
    (posts/init!)))
