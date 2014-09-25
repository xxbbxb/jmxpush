(defproject jmxpush "0.1.0-SNAPSHOT"
  :description "A JMX connector for plain-text graphite protocol"
  :url "https://github.com/xxbbxb/jmxpush"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jmx "0.2.0"]
                 [clj-yaml "0.4.0"]]
  :main jmxpush.core)
