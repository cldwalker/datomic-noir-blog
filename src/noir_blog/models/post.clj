(ns noir-blog.models.post
  (:require [datomic-simple.core :as ds]
            [clj-time.core :as ctime]
            [clj-time.format :as tform]
            [clj-time.coerce :as coerce]
            [clojure.string :as string]
            [noir-blog.models.user :as users]
            [noir.validation :as vali]
            [noir.session :as session])
  (:import com.petebevin.markdown.MarkdownProcessor))

(def posts-per-page 10)
(def date-format (tform/formatter "MM/dd/yy" (ctime/default-time-zone)))
(def time-format (tform/formatter "h:mma" (ctime/default-time-zone)))
(def mdp (MarkdownProcessor.))
(def model-namespace :post)
(def schema (ds/build-schema model-namespace
      [[:title :string]
       [:body :string]
       [:moniker :string]
       [:username :string]
       [:date :string]
       [:tme :string]]))
(ds/create-model-fns model-namespace)

;; Gets
(defn total []
  (count (find-all)))

(defn moniker->post [moniker]
  (find-first {:moniker moniker}))

(defn get-page [page]
  (let [page-num (dec (Integer. page)) ;; make it 1-based indexing
        prev (* page-num posts-per-page)]
    (take posts-per-page (drop prev (find-all)))))

(defn get-latest []
  (get-page 1))

;; Mutations and checks

(defn gen-moniker [title]
  (-> title
    (string/lower-case)
    (string/replace #"[^a-zA-Z0-9\s]" "")
    (string/replace #" " "-")))

(defn new-moniker? [moniker]
  (->> (map :moniker (find-all))
    (some #{moniker})
    not))

(defn perma-link [moniker]
  (str "/blog/view/" moniker))

(defn edit-url [{:keys [id]}]
  (str "/blog/admin/post/edit/" id))

(defn md->html [text]
  (. mdp (markdown text)))

(defn wrap-moniker [{:keys [title] :as post}]
  (let [moniker (gen-moniker title)]
    (-> post
      (assoc :moniker moniker))))
    
(defn wrap-time [post]
  (let [ts (ctime/now)]
    (-> post
      (assoc :date (tform/unparse date-format ts))
      (assoc :tme (tform/unparse time-format ts)))))

(defn prepare-new [post]
  (-> post
    (assoc :username (users/me))
    (wrap-time)
    (wrap-moniker)))

(defn valid? [{:keys [title body]}]
  (vali/rule (vali/has-value? title)
             [:title "There must be a title"])
  (vali/rule (new-moniker? (gen-moniker title))
             [:title "That title is already taken."])
  (vali/rule (vali/has-value? body)
             [:body "There's no post content."])
  (not (vali/errors? :title :body)))

;; Operations

(defn add! [post]
  (when (valid? post)
    (create (prepare-new post))))

(defn edit! [{:keys [id] :as post}]
  (update id (wrap-moniker (dissoc post :id))))

(defn remove! [id]
  (ds/delete id))
