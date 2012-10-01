(ns noir-blog.models.user
  (:require [noir-blog.datomic :as db]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

(def model-namespace :user)
(def schema (db/build-schema model-namespace [[:username :string] [:password :string]]))

;; Gets

(defn all []
  (db/all '[:find ?e :where [?e :user/username]]))

(defn get-username [username]
  (db/local-find-first '[:find ?e :in $ ?name :where [?e :user/username ?name]] username))
    
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

(defn- create [user]
  (db/create model-namespace (prepare user)))

(defn update [attr]
  (if-let [user (get-username (:username attr))]
    (db/update model-namespace (:id user) attr)))

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
      (create user))))

; TODO: updating username doesn't work
(defn edit! [{:keys [username old-name password]}]
  (let [user {:username username :password password}]
    (if (= username old-name)
      (when (valid-psw? password)
        (-> user (prepare) (update)))
      (update user))))

(defn remove! [username]
  (if-let [user (get-username username)]
    (db/delete (:id user))))

(defn init! []
  (db/transact! schema)
  (create {:username "admin" :password "admin"}))
