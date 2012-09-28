(ns noir-blog.models.user
  (:require [noir-blog.datomic :as db]
            ;[simpledb.core :as db]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

;; Gets

(defn all []
  (db/find-all '[:find ?e :where [?e :user/username]]))

(defn get-username [username]
  (if-let [user (db/find-first '[:find ?e :in $ ?name :where [?e :user/username ?name]] username)]
    (db/map-keys user #(keyword (name %)))))
    
(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

;; Mutations and Checks

(defn prepare [{password :password :as user}]
  (assoc user :password (crypt/encrypt password)))

(defn valid-user? [username]
  (vali/rule (not (get-username username))
             [:username "That username is already taken"])
  (vali/rule (vali/min-length? username 3)
             [:username "Username must be at least 3 characters."])
  (not (vali/errors? :username :password)))

(defn valid-psw? [password]
  (vali/rule (vali/min-length? password 5)
             [:password "Password must be at least 5 characters."])
  (not (vali/errors? :password)))

;; Operations

(defn- store! [user]
  @(db/transact [user]))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (get-username username)]
    (if (and stored-pass 
             (crypt/compare password stored-pass))
      (do
        (session/put! :admin true)
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))

(defn add! [{:keys [username password] :as user}]
  (when (valid-user? username)
    (when (valid-psw? password)
      (store! (db/build-attr :user (prepare user))))))

(defn edit! [{:keys [username old-name password]}]
  (let [user {:username username :password password}]
    (if (= username old-name)
      (when (valid-psw? password)
        (-> user (prepare) (store!)))
      (add! user))))

; TODO
(defn remove! [username]
  ;(db/update! :users dissoc username))
  )

(def user-schema (db/build-schema :user [[:username :string] [:password :string]]))

(defn init! []
  @(db/transact user-schema)
  ; can't use add! b/c noir.validation/*errors* needs ring middleware to initialize it
  (store! (db/build-attr :user (prepare {:username "admin" :password "admin"}))))
