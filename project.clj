(defproject blogger2cryogen "0.2.0"
  :description "Export Blogger content to Cryogen"
  :url "https://github.com/sanel/blogger2cryogen"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [net.sf.jtidy/jtidy "r938"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all}}
  :repl-options {:port 7889}
  :main blogger2cryogen.core)

