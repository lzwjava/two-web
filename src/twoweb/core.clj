(ns twoweb.core
  (:use [compojure.core :only (GET PUT POST defroutes)])
  (:require (compojure handler route)
            [ring.util.response :as response]
            [clojure.java.shell]
            [clj-http.client :as client]
            [clojure.data.json :as json]))
(import '(java.io StringReader BufferedReader))

(declare md5)
(def project-name "lzw_chat")
(def work-dir (str "/home/lzw/workspace/" project-name))
(def room-num-path "/home/lzw/android/apache-tomcat/webapps/room")
(def apkRoomPath (str work-dir "/res/raw/room"))
(def output-dir (str "/home/lzw/android/apache-tomcat/webapps/apk"))

(defn md5-encode [num]
  (md5 (str "ZhaiYingLoveMe" num)))

(defn encode [num]
  (str "two-" (md5-encode num)))

(defn make-app-of-num [num]
  (let [outputApk (str (encode num) ".apk")
        output-path (str output-dir "/" outputApk)]
    (spit apkRoomPath (md5-encode num))
    (let [{:keys [exit]}
          (clojure.java.shell/sh "ant" "release" "-buildfile" (str work-dir "/build.xml"))]
      (if (zero? exit)
        (let [apkFile (java.io.File. (str work-dir "/bin/lzw_chat-release.apk"))
              outputFile (java.io.File. output-path)
              output-dir-file (java.io.File. output-dir)]
          (if-not (.exists output-dir-file)
            (.mkdirs output-dir-file))
          (println (.renameTo apkFile outputFile))
          (spit room-num-path (str (+ num 1)))
          outputApk)
        "error"))))


(defn shortenUrl [url]
  (let [jsonStr (client/get "https://api.weibo.com/2/short_url/shorten.json"
                            {:query-params {"access_token" "2.00_hkjqB7sDeMB60016c93a90cPv4H"
                                            "url_long"  url}})
        body (:body jsonStr)
        jsonBody (json/read-str body)
        urls (get (get jsonBody "urls") 0)
        short (get urls "url_short")]
    short))
;(shortenUrl "http://www.baidu.com")

(defn make-app []
  (let [str1 (slurp room-num-path)
        num (read-string str1)
        apk-path (make-app-of-num num)]
    (spit room-num-path (str (inc num)))
    (let [apk-url (str "http://114.215.107.217:8080/apk/" apk-path)]
      (shortenUrl apk-url))))

;(make-app)

(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
        (doto (java.security.MessageDigest/getInstance "MD5")
          (.reset)
          (.update (.getBytes token)))]
    (.toString
      (new java.math.BigInteger 1 (.digest hash-bytes))     ; Positive and the size of the number
      16)))
;(md5 "hi")


(defroutes
  app*
  (GET "/gen" []
       {:status 200
        :headers {"Access-Control-Allow-Origin" "*"}
        :body   (dosync
                  (make-app))})
  (compojure.route/not-found "Sorry,there's nothing here!"))

(def app (compojure.handler/api app*))

(use '[ring.adapter.jetty :only (run-jetty)])
(def server (ref 'a))
(defn run-server []
  (dosync
    (ref-set
      server
      (run-jetty
        #'app
        {:host "127.0.0.1" :port 8090 :join? false}))))

(defn -main [& args]
  (run-server))
;(.stop @server)
;(-main)