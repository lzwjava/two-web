(defproject shortener "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring "1.0.1"]]
  :dev-dependencies [[lein-ring "0.4.5"]]
  :ring {:handler shortener.core/app}
  :main shortener.core)
