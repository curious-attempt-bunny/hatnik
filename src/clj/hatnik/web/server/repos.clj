(ns hatnik.web.server.repos
  (:require [ring.util.response :as resp]
            [compojure.core :refer :all]
            [tentacles.repos :as repos]))

(defn get-user [req]
  (-> req :session :user))

()

(defn all-repos [user]
  (let [options {:oauth_token (:user-token user) :per-page 100}
        repos   (->> (repos/repos options)
                   (map #(do (prn %) (identity %)))
                   (map :full_name)
                   (remove nil?))]
    (prn "*user*" user)
    (prn "*options*" options)
    (resp/response
     {:result :ok
      :repos repos})))

(defroutes repos-api
  (GET "/" req (all-repos (get-user req))))
