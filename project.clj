(defproject twoweb "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring "1.0.1"]
                 [clj-http "0.9.1"]
                 [org.clojure/data.json "0.2.4"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler twoweb.core/app}
  :main twoweb.core)
