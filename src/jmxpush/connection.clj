(ns jmxpush.connection
  (:import (java.net Socket)
           (java.io PrintWriter)))

(defn connect [server]
  (let [socket (Socket. (:host server) (:port server))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:out out :socket socket})]
    conn))

(defn close [conn]
  (.close (:out @conn))
  (.close (:socket @conn)))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.checkError)))