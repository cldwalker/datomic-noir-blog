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

(defn find-all [query & args]
  (->> (apply where query args) flatten (map entity->map))) 

(defn find-first [query & args]
  (first (apply find-all query args)))

(defn init []
  (d/delete-database uri)
  (d/create-database uri)
  (reset! connection (d/connect uri)))

(defn transact [tx]
  (d/transact @connection tx))

(defn transact! [tx]
  (prn "Transacting..." tx)
  @(transact tx))

(defn num-id [id]
  (Long. id))

(defn find-id [id]
  (let [entity (d/entity (db) (num-id id))]
    (if-not (empty? entity) (entity->map entity))))

(defn delete [& ids]
   (transact! (map #(vec [:db.fn/retractEntity (num-id %)]) ids)))

(defn update [id attr]
  (transact! [(merge attr {:db/id (num-id id)})]))

(defn build-schema-attr [attr-name value-type & options]
  (let [cardinality (if (some #{:many} options)
                      :db.cardinality/many
                      :db.cardinality/one)
        fulltext    (not (boolean (some #{:nofulltext} options))) 
        history     (boolean (some #{:nohistory} options))
        index       (not (boolean (some #{:noindex} options)))]
    
    {:db/id           (d/tempid :db.part/db)
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

(defn map-keys [oldmap kfn]
  (->> oldmap
    (map (fn [[key val]] [(kfn key) val])) flatten (apply hash-map)))

(defn localize-attr [attr]
  (map-keys attr #(keyword (name %))))

(defn all [query]
  (map localize-attr (find-all query)))

(defn local-find-id [id]
  (if-let [m (find-id id)] (localize-attr m)))

(defn namespace-keys [attr nsp]
  (map-keys attr #(keyword (name nsp) (name %))))

(defn build-attr [nsp attr]
  (->> (namespace-keys attr nsp)
    (merge {:db/id (d/tempid :db.part/user)})))

; for repl-testing purposes
(def user-schema (build-schema :user [[:username :string]  [:password :string]]))
(def user-data [{:username "Coolio" :password "Gang"}  {:username "Snoop" :password "Sucks"}])

(defn user-init []
  (transact! user-schema)
  (transact! (map #(build-attr :user %) user-data)))
