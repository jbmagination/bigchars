(ns letters
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(def whitespace
  {\space [" "
           " "
           " "
           " "
           " "
           " "
           " "
           " "
           " "
           " "
           " "]})

(defn spaces [n]
  (take n (repeat (whitespace \space))))

(defn print-letter [c characters]
  (println
   (str/join
    "\n"
    (get characters c))))

(defn load-font* [font]
  (let [characters (edn/read-string
                    (slurp
                     (or (io/resource (str font ".edn"))
                         (io/input-stream (io/file (str font ".edn"))))))]
    (merge
     characters
     whitespace)))

;;(def load-font (memoize load-font*))
(def load-font load-font*)

(defn big-character-strings [{:keys [font sentence]}]
  (let [characters (load-font font)
        sentence (str/replace sentence " " "  ")
        chars (interleave (spaces (count sentence))
                          (mapv
                           characters
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

(defn on-message-received [message channel]
  (let [incoming (.getContentDisplay message)
        _ (log/info "Incoming message ===== " incoming)
        pieces (str/split incoming #"\s+" 3)
        _ (log/info "Pieces ----> " (into [] pieces))
        command (get-command pieces)
        _ (log/info "Command is: [" command "]")]
    (when (= command "/big-chars")
      (let [font (get-font pieces)
            _ (log/info "Font ==== " font)
            sentence (get-sentence pieces)
            _ (log/info "Sentence =-----= " sentence)
            reply (str "\n```\n" (big-character-strings {:font font :sentence sentence}) "\n```\n")
            _ (log/info "Reply is ---> " reply)]
        (.sendMessage channel reply)
        (.deleteMessageById channel (.getId message))))))

(defn bigchar-listener []
  (proxy [net.dv8tion.jda.core.hooks.ListenerAdapter] []
    (onMessageReceived [^net.dv8tion.jda.core.events.message.MessageReceivedEvent message-received-event]
      (on-message-received (.getMessage message-received-event)
                           (.getChannel message-received-event)))))

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
