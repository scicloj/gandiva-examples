(defproject gandiva-examples "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.arrow/arrow-vector "0.14.0-SNAPSHOT"]
                 [org.apache.arrow/arrow-memory "0.14.0-SNAPSHOT"]
                 [org.apache.arrow/arrow-format "0.14.0-SNAPSHOT"]
                 [org.apache.arrow.gandiva/arrow-gandiva "0.14.0-SNAPSHOT"]
                 [org.slf4j/slf4j-api "1.7.26"]
                 [org.slf4j/slf4j-log4j12 "1.7.26"]]
  :main ^:skip-aot gandiva-examples.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})



