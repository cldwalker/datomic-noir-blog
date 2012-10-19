(ns noir-blog.models
  (:require [datomic-simple.core :as ds]
            [noir-blog.models.user :as user]
            [noir-blog.models.post :as post]))

(def uri "datomic:mem://noir-blog")

(defn initialize [opts]
  (ds/start (merge opts {
             :uri uri
             :schemas [user/schema post/schema]
             :seed-data [user/seed-data]})))
