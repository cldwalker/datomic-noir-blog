(ns noir-blog.models
  (:require [datomic-simple.core :as db]
            [noir-blog.models.user :as user]
            [noir-blog.models.post :as post]))

(def uri "datomic:mem://noir-blog")

(defn initialize []
  (db/start {:uri uri
             :schemas [user/schema post/schema]
             :seed-data [user/seed-data]}))
