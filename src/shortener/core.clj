(ns shortener.core
  (:use [compojure.core :only (GET PUT POST defroutes)])
  (:require (compojure handler route)
            [ring.util.response :as response]))

(def mappings (ref {}))
(def counter (atom 0))
(dosync
 (alter mappings assoc "hi" "http://www.baidu.com")) 

(defroutes app*
  (GET "/" request "it's ")
  (GET "/:id" [id] (redirect id))
  (GET "/list/" [] (flatten (interpose "<br>" (map (partial interpose "&nbsp;&nbsp;") (vec @mappings)))))
  (PUT "/:id" [id url]
       (let [url (addHttp url)]
         (retain url id)))
  (POST "/" [url]
        (if (empty? url)
          {:status 400 :body "no 'url' parameter provided"}
          (retain url))))

(interpose "<br>" (flatten (map (partial interpose "&nbsp;&nbsp;") (vec {:a 1 :b 2}))))
;if the url isn't start with http:// ,the redirect function don't work.
(defn addHttp
  [url]
  (if (.startsWith url "http://")
    url
    (str "http://" url)))

(defn redirect
  [id] 
  (let [url (@mappings id)]
    (if (not (nil? url))
      (response/redirect url)
      {:status 404
       :body (format "haven't assign %s" id)} )))

;(redirect "hi")
;(response/redirect "http://www.baidu.com")

(defn shorten!
  [url id]
  (dosync
   (when-not (@mappings id)
     (alter mappings assoc id url)
     id)))

(defn retain
  ([url id]
      (if-let [id (shorten! url id)]
        {:status 201
         :headers {"Location" id}
         :body (format "url %s assigned to %s" url id)}
        {:status 409
         :body (format "%s is already taken" id)}))
  ([url]
     (let [id (swap! counter inc)
           id (Long/toString id)]
       (or (shorten! url id)
           (recur url)))))

(def app (compojure.handler/api app*))

(use '[ring.adapter.jetty :only (run-jetty)])
(.stop server)
(def server (run-jetty #'app {:host "127.0.0.1" :port 8083 :join? false}))

(defn run-server []
  ;(use 'ring.adapter.jetty) 
  server)

(defn -main [& args]
  (run-server))

