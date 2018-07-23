(ns letters
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

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
  (first  (take 1 (drop 1 pieces))))

(defn get-sentence [pieces]
  (first (drop 2 pieces)))

(defn on-message-received [message channel]
  (let [incoming (.getContentDisplay message)
        pieces (str/split incoming #"\s+" 3)
        command (get-command pieces)]
    (when (= command "/big-chars")
      (let [font (get-font pieces)
            sentence (get-sentence pieces)
            reply (big-character-strings {:font font :sentence sentence})]
        (.sendMessage channel reply)
        (.deleteMessageById channel (.getId message))))))

(str/split "a" #"\s+" 3)


(defn bigchar-listener []
  (proxy [net.dv8tion.jda.core.hooks.ListenerAdapter] []
    (onMessageReceived [this ^net.dv8tion.jda.core.events.message.MessageReceivedEvent message-received-event]
      (on-message-received (.getMessage message-received-event)
                           (.getChannel message-received-event)))))

(defn get-token []
  (let [token (System/getenv "BOT_TOKEN")]
    (log/info "bot token ====== " token)
    (or  token
         (throw (IllegalStateException. "BOT_TOKEN env var is NOT set!!")))))

(defn -main [& args]
  (doto (net.dv8tion.jda.core.JDABuilder. net.dv8tion.jda.core.AccountType/BOT)
    (.setToken (get-token))
    (.addEventListener (to-array [(bigchar-listener)]))
    (.buildBlocking)))
