(defproject letters "0.1.0-SNAPSHOT"

  :description "a discord bot to output big text characters"

  :url ""

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [net.dv8tion/JDA "3.7.1_386" :exclusions [club.minced/opus-java]]
                 [log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]]

  :repositories [["jcenter" {:url "https://jcenter.bintray.com"}]]

  :aot :all
  :main letters)
