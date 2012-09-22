(ns noir-blog.datomic
  (:require [datomic.api :as d]))

(def uri "datomic:mem://noir-blog")
(def connection (atom nil))

(defn db [] (d/db @connection))

(defn q [query & args]
  (apply d/q query (db) args))

(defn where [query & args]
  (->> (apply q query args)
    (mapv (fn [items]
      (mapv #(d/entity (db) %) items)))))

(defn entity->map [e]
  (merge (select-keys e (keys e))
         {:db/id (:db/id e)}))

(defn where-all [query & args]
  (->> (apply where query args) flatten (map entity->map))) 

(defn init []
  (d/delete-database uri)
  (d/create-database uri)
  (reset! connection (d/connect uri)))

(defn transact [tx]
  (prn "Adding..." tx)
  (d/transact @connection tx))

(defn tempid []
  (d/tempid :db.part/db))

(defn build-schema-attr [attr-name value-type & options]
  (let [cardinality (if (some #{:many} options)
                      :db.cardinality/many
                      :db.cardinality/one)
        fulltext    (not (boolean (some #{:nofulltext} options))) 
        history     (boolean (some #{:nohistory} options))
        index       (not (boolean (some #{:noindex} options)))]
    
    {:db/id           (tempid)
     :db/ident        attr-name
     :db/valueType    (keyword "db.type" (name value-type))
     :db/cardinality  cardinality
     :db/index        index
     :db/fulltext     fulltext
     :db/noHistory    history
     :db.install/_attribute :db.part/db}))
    

(defn build-schema [nsp attrs]
  (map #(apply build-schema-attr
               (keyword (name nsp) (name (first %))) (rest %))
       attrs))

(def user-schema (build-schema :user [[:username :string]  [:password :string]]))
(def user-data [{:user/username "Coolio" :user/password "Gang"}  {:user/username "Snoop" :user/password "Sucks"}])

(defn user-init []
  @(transact user-schema)
  @(transact (map #(assoc % :db/id  (tempid)) user-data)))
