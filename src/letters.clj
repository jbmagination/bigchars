(ns letters
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [discord.bot :as bot]
            [discord.http :as http]))

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

(defn lines [{:keys [font sentence]}]
  (let [characters (load-font font)
        sentence (str/replace sentence " " "  ")
        chars (interleave (spaces (count sentence))
                          (mapv
                           characters
                           sentence))]
    (reduce
     (fn [v s]
       (conj v s))
     []
     (apply map
            (fn [& args]
              (apply str args))
            chars))))


(defn get-font [pieces]
  (first (take 1 pieces)))

(defn get-sentence [pieces]
  (first (drop 1 pieces)))

(bot/defcommand bc-write [client message]
  "Tells the bot to echo back the content of your message and then deletes the user's original message."
  (let [pieces (str/split message #"\s+" 1)
        font (get-font pieces)
        sentence (get-sentence pieces)
        lines (lines {:font font :sentence sentence})])
  (bot/say "```")
  (mapv bot/say lines)
  (bot/say "```")
  (bot/delete message))

(defn -main
  "Creates a new discord bot and supplies a series of extensions to it."
  [& args]
  (bot/with-extensions
    "BigCharacters" "/"
    :big-chars @'bc-write))
