(ns noir-blog.models.post
  (:require [noir-blog.datomic :as db]
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
(def schema (db/build-schema model-namespace
                             [[:title :string] [:body :string] [:moniker :string]
                             [:username :string] [:date :string] [:tme :string]]))


;; Gets

(defn all []
  (db/all '[:find ?e :where [?e :post/title]]))

(defn total []
  (count (all)))

(defn moniker->post [moniker]
  (if-let [post (db/find-first '[:find ?e :in $ ?moniker :where [?e :post/moniker ?moniker]] moniker)]
    (db/localize-attr post)))

(defn get-page [page]
  (let [page-num (dec (Integer. page)) ;; make it 1-based indexing
        prev (* page-num posts-per-page)]
    (take posts-per-page (drop prev (all)))))

(defn get-latest []
  (get-page 1))

;; Mutations and checks

(defn gen-moniker [title]
  (-> title
    (string/lower-case)
    (string/replace #"[^a-zA-Z0-9\s]" "")
    (string/replace #" " "-")))

(defn new-moniker? [moniker]
  (->> (map :moniker (all))
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
    (db/transact! [(db/build-attr model-namespace (prepare-new post))])))

(defn edit! [{:keys [id] :as post}]
  (db/update id (db/namespace-keys (wrap-moniker (dissoc post :id)) model-namespace)))

(defn remove! [id]
  (db/delete id))

(defn init! []
  (db/transact! schema))
