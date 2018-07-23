(defproject letters "0.1.0-SNAPSHOT"

  :description "a discord bot to output big text characters"

  :url ""

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [net.dv8tion/JDA "3.7.1_386" :exclusions [club.minced/opus-java]]]

  :repositories [["jcenter" {:url "https://jcenter.bintray.com"}]]

  :aot :all
  :main letters)
