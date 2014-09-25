(ns jmxpush.core
  (:require [clojure.java.jmx :as jmx]
            [clj-yaml.core :as yaml]
            [jmxpush.connection :as connection]
            [clojure.pprint :refer (pprint)]
            [clojure.string :refer (join)])
  (:gen-class))

(defn- get-connection-helper
  [host port]
  (doto (connection/connect {:host host :port port})))

(defn run-queries
  [yaml]
  (let [{:keys [jmx queries]} yaml]
    (->>
      (jmx/with-connection jmx
        (doall
          (for [query queries
              name (jmx/mbean-names (:obj query))
              attr (:attr query)]
            {:metric    (join \. (filter identity [(:prefix jmx) (:host jmx) (get query :metric name) attr]))
             :value     (jmx/read name attr)
             :timestamp (quot (System/currentTimeMillis) 1000)})))
      (mapcat (fn [{:keys [metric value] :as event}]
        (if (map? value)
          (for [[k v] value]
           (assoc event
             :metric (str metric \. (name k))
             :value v))
          [event]))))))

(defn run-configuration
  [yaml]
  (let [{{:keys [host port]} :push-to} yaml
        conn (if port
                (get-connection-helper host port)
                (get-connection-helper host 2003))
        events (run-queries yaml)]
    (if (.checkError (connection/write
        conn
        (join "\n"
          (for [event events]
            (join " " (vals event))))))
      (throw (Exception. "Unable to write")))
    (connection/close conn)))

(defn start-config
  "Takes a path to a yaml config, parses it, and runs it in a loop"
  [config]
  (let [yaml (yaml/parse-string (slurp config))]
    (pprint yaml)
    (future
      (while true
        (def r ".")
        (try
          (run-configuration yaml)
          (catch Exception e
            (def r "e")))
        (print r)
        (flush)
        (Thread/sleep (* 1000 (-> yaml :push-to :interval)))))))



(defn -main
  [& args]
  (doseq [arg args]
    (start-config arg)
    (println "-- Started --")))