(ns letters
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn upper-case [c]
  (first (str/upper-case c)))

(defn lower-case [c]
  (first (str/lower-case c)))

(defn get-character [characters c]
  (or (characters c)
      (characters (upper-case c))
      (characters (lower-case c))))

(defn spaces [characters n]
  (take n (repeat (characters \space))))

(defn print-letter [c characters]
  (println
   (str/join
    "\n"
    (get characters c))))

(defn load-font* [font]
  (edn/read-string
   (slurp
    (or (io/resource (str font ".edn"))
        (io/input-stream (io/file (str font ".edn")))))))

(def load-font (memoize load-font*))
;;(def load-font load-font*)

(defn big-character-strings [{:keys [font sentence]}]
  (let [characters (load-font font)
        sentence (str/replace sentence " " "  ")
        chars (interleave (spaces characters (count sentence))
                          (mapv
                           (partial get-character characters)
                           sentence))]
    (str/join
     "\n"
     (apply mapv
            (fn [& args]
              (apply str args))
            chars))))

(defn get-command [pieces]
  (first (take 1 pieces)))

(defn get-font [pieces]
  (first (take 1 (drop 1 pieces))))

(defn get-sentence [pieces]
  (first (drop 2 pieces)))

(defn on-message-received [message channel user]
  (let [incoming (.getContentDisplay message)
        pieces (str/split incoming #"\s+" 3)
        command (get-command pieces)]
    (when (= command "/big-chars")
      (let [font (get-font pieces)
            sentence (get-sentence pieces)
            reply (str/join
                   "\n"
                   ["```"
                    (big-character-strings {:font font :sentence sentence})
                    "```"
                    (format "From [%s]" (.getName user))])]
        (if (> (count reply) 2000)
          (-> channel
              (.sendMessage "Message exceeds discord's 2000 character limit!")
              (.queue))
          (do
            (-> channel
                (.sendMessage reply)
                (.queue))
            (-> (.delete message)
                (.queue))))))))

(defn bigchar-listener []
  (proxy [net.dv8tion.jda.core.hooks.ListenerAdapter] []
    (onMessageReceived [^net.dv8tion.jda.core.events.message.MessageReceivedEvent message-received-event]
      (on-message-received (.getMessage message-received-event)
                           (.getChannel message-received-event)
                           (.getAuthor message-received-event)))))

(defn get-token []
  (let [token (System/getenv "BOT_TOKEN")]
    (log/info "bot token ====== " token)
    (or  token
         (throw (IllegalStateException. "BOT_TOKEN env var is NOT set!!")))))

(defn handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello from BigChars discord bot!"})

(defn -main [& args]

  (run-jetty #'handler {:port (Integer/parseInt (get (System/getenv) "PORT" "5000")) :join? false})

  (doto (net.dv8tion.jda.core.JDABuilder. net.dv8tion.jda.core.AccountType/BOT)
    (.setToken (get-token))
    (.addEventListener (to-array [(bigchar-listener)]))
    (.buildBlocking)))
