(ns
  ^{:author "Sanel Zukan"
    :doc "Utility to migrate Blogger posts to Cryogen static site generator."}
  blogger2cryogen.core
  (:gen-class)
  (:require [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.string :as s]
            [clojure.xml :as xml]
            [clojure.tools.cli :refer [parse-opts]])
  (:import org.w3c.tidy.Tidy
           [java.io File ByteArrayInputStream ByteArrayOutputStream]))

(def ^:const cli-options
  [["-i" "--in  DUMP"    "XML dump from Blogger"]
   ["-o" "--out FOLDER"  "Extract posts to FOLDER"]
   ["-n" "--no-struct"   "Do not create FOLDER/pages and FOLDER/posts subfolders"]
   ["-t" "--tidy"        "Tidy extracted HTML"]
   ["-h" "--help"        "Display this help"]])

(defn- usage
  "Display command line options."
  [options]
  (->> ["Usage: blogger2cryogen [options]"
        "Export your Blogger posts to Cryogen static site generator."
        options]
       (s/join \newline)
       println))

(defn- post?
  "Check input if is post. Blogger will append '#post' to <category> term."
  [^String s]
  (and (string? s)
       (= "post" (-> s (.split "#") second))))

(defn- extract-sublink
  "From full URL, get the last component. It will be used to construct filename."
  [^String s]
  (when-let [^String name (-> s (.split "/") last)]
    (first (.split name "\\."))))

(defn- escape-dquotes
  "Escape double quotes. Mainly to fix title so we don't get malformed metadata."
  [^String s]
  (.replaceAll s "\"" "\\\\\""))

(defn- tidy-html
  "Pass html string through JTidy for fixing it up if necessary."
  [^String s]
  (let [out (ByteArrayOutputStream.)
        in  (-> s .getBytes ByteArrayInputStream.)]
    (doto (Tidy.)
      (.setXHTML true)
      (.setSpaces 2)
      (.setPrintBodyOnly true)
      (.setIndentContent true)
      (.parse in out))
    (str out)))

(defn- create-filename
  "Build filename based on publication date and full post link."
  [^String destination ^String date ^String link]
  (let [date  (-> date (.split "T") first)
        title (extract-sublink link)]
    (str destination "/" date "-" title ".html")))

(defn- fail-unless
  "Fail with some output."
  [expr s]
  (when-not expr
    (println s)
    (System/exit 1)))

(defn- ensure-structure
  "Ensure folders are created."
  [base]
  (-> (format "%s/pages" base) File. .mkdirs)
  (-> (format "%s/posts" base) File. .mkdirs))

(defn- do-dump
  "Actual content creator."
  [path title content tags]
  (let [header (format "{:title \"%s\"\n :layout :post\n :tags %s}\n\n"
                       (escape-dquotes title)
                       (vec tags))]
    (spit path (str header content))
    (println "Wrote" path)))

(defn- print-epilogue
  "Print extraction epilogue."
  [counter]
  (println
   (if (< counter 1)
     "Nothing exported."
     (str "Done! Exported " counter " post" (if (> counter 1) "s") "."))))

(defn process
  "Main runner that consumes parsed args options."
  [options]
  (let [in        (:in options)
        out       (:out options)
        tidy?     (:tidy options)
        struct?   (not (:no-struct options))
        posts-dir (if struct?
                      (format "%s/posts" out)
                      out)]
    (fail-unless in "Missing input xml dump. Use '--in' option.")
    (fail-unless out "Missing output folder. Use '--out' option.")
   
    (when struct?
      (ensure-structure out))
    
    (loop [nodes (-> in xml/parse :content)
           counter 0]
      (if-let [i (first nodes)]
        (let [zipper (zip/xml-zip i)
              term   (first (zx/xml-> zipper :category (zx/attr :term)))]
          ;; only take into accout posts, not comments or other stuff
          (when (post? term)
            (let [;; tags are list so we can accept multiple tags
                  tags (next (zx/xml-> zipper :category (zx/attr :term)))
                  date (first (zx/xml-> zipper :published zx/text))
                  link (first (zx/xml-> zipper :link (zx/attr= :rel "alternate") (zx/attr :href)))
                  content (first (zx/xml-> zipper :content zx/text))
                  title   (first (zx/xml-> zipper :title zx/text))]
              (do-dump
               (create-filename posts-dir date link)
               title
               (if tidy? (tidy-html content) content)
               tags)))
          (recur (next nodes) (inc counter)))
        (print-epilogue counter)))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (if (or (empty? options)
            (:help options))
      (usage summary)
      (process options))))
