(ns jmxpush.core
  (:require [clojure.java.jmx :as jmx]
            [clj-yaml.core :as yaml]
            [jmxpush.connection :as connection]
            [clojure.pprint :refer (pprint)])
  (:gen-class))

(defn- get-connection-helper
  [host port]
  (doto (connection/connect {:host host :port port})))

(let [get-connection-helper (memoize get-connection-helper)]
  (defn get-connection
    ([host]
     (get-connection-helper host 2003))
    ([host port]
     (get-connection-helper host port))))

(defn run-queries
  [yaml]
  (let [{:keys [jmx queries]} yaml]
    (->> (jmx/with-connection jmx
           (doall
             (for [{:keys [obj attr]} queries
                   name (jmx/mbean-names obj)
                   attr attr]
               {:metric (str (:prefix jmx) "." service "." (:host jmx))
                :value (jmx/read name attr)})))
         (mapcat (fn [{:keys [metric value] :as event}]
                   (if (map? value)
                     (for [[k v] value]
                       (assoc event
                              :metric (str metric (name k))
                              :value v))
                     [event]))))))

(defn run-configuration
  "Takes a parsed yaml config, runs the queries, and posts the results to riemann"
  [yaml]
  (let [{{:keys [host port]} :push-to} yaml
        conn (if port
               (get-connection host port)
               (get-connection host))
        events (run-queries yaml)]

    (print ".")
    (flush)
    (pprint events)))

(defn start-config
  "Takes a path to a yaml config, parses it, and runs it in a loop"
  [config]
  (let [yaml (yaml/parse-string (slurp config))]
    (pprint yaml)
    (future
      (while true
        (try
          (run-configuration yaml)
          (Thread/sleep (* 1000 (-> yaml :push-to :interval)))
          (catch Exception e
            (.printStackTrace e)))))))

(defn -main
  [& args]
  (doseq [arg args]
    (start-config arg)
    (println "Started monitors")))
