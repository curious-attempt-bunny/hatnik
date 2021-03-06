(ns hatnik.web.server.schema
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [cheshire.generate :as gen]
            [schema.utils :as u]))

(defn string-of-length
  "Create schema that validates string length."
  [min max]
  (s/both String
          (s/pred #(<= min (count %) max)
                  (symbol (format "length-from-%s-to-%s?" min max)))))

(def Id
  "Schema for validaing that string matches id.
  We restrict ids to be up to 32 symbols consisting only from
  alphanumeric values. MongoDB uses such ids."
  (s/both (string-of-length 1 32)
          (s/pred #(re-matches #"^[a-zA-Z0-9]+$" %)
                  'alphanumeric?)))

(def Email
  "Schema for validation emails. We restrict email to be 128
  without no good reason actually."
  (s/both (string-of-length 1 128)
          (s/pred #(re-matches #"(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$" %)
                  'email-format?)))

(def Project
  "Schema for project. Project has only 1 field - name in API."
  {:name (string-of-length 1 128)})

(def EmailAction
  "Schema for EmailAction."
  {:project-id Id
   :library (string-of-length 1 128)
   :type (s/eq "email")
   :address Email
   :template (string-of-length 1 2000)})

(def Action
  "Schema for action. Essentially it is the union of all actions."
  (s/conditional
   #(= (:type %) "email") EmailAction))

(defmacro ensure-valid
  "Validates object using given schema and executes body if valid.
  If not valid - returns 400 response."
  [schema obj & body]
  `(if-let [error# (s/check ~schema ~obj)]
     {:body {:result :error
             :message "Invalid request format"
             :validation-error error#}
      :status 400}
     (do ~@body)))


; Add encoders to cheshire so validation errors can be
; encoded to string and sent in response.
(gen/add-encoder schema.utils.ValidationError
                 (fn [obj gen]
                   (gen/encode-seq (u/validation-error-explain obj) gen)))

(gen/add-encoder Class
                 (fn [obj gen]
                   (gen/encode-str (.getName obj) gen)))
(comment

  (s/check Id "32")

  )
