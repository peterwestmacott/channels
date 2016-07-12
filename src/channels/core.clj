(ns channels.core
  (:require [yada.yada :refer [listener] :as yada]
            [clojure.tools.logging :refer :all]
            [bidi.vhosts :refer [make-handler vhosts-model]]
            [bidi.bidi :refer [tag]]
            [yada.resources.webjar-resource :refer [new-webjar-resource]]
            [channels.index :as index]
            [channels.mvc :as mvc]
            ))


(defn index-routes []
  ["/index" (yada/resource
              {:methods
               {:get
                {:produces {:media-type #{"application/edn;q=0.9"
                                          "application/json"}}
                 :response index/index}}})])

(defn hello-routes []
  ["/hello" (yada/handler "Hello World!\n")])

(defn make-routes []
  ["/channels" [["/index" (yada/resource
                            {:methods
                             {:get
                              {:produces {:media-type #{"application/edn;q=0.9"
                                                        "application/json"}}
                               :response (index/index-for mvc/description)}}})]
                mvc/routes]])

(def routes
  [""
   [
    (make-routes)
    ["/api" (-> (make-routes)
                ;; Wrap this route structure in a Swagger
                ;; wrapper. This introspects the data model and
                ;; provides a swagger.json file, used by Swagger UI
                ;; and other tools.
                (yada/swaggered
                  {:info {:title "Hello World!"
                          :version "1.0"
                          :description "An API on the classic example"}
                   :basePath "/api"})
                ;; Tag it so we can create an href to this API
                (tag :edge.resources/api))]

    ;; Swagger UI
    ["/swagger" (-> (new-webjar-resource "/swagger-ui" {:index-files ["index.html"]})
                    ;; Tag it so we can create an href to the Swagger UI
                    (tag :edge.resources/swagger))]

    [true (yada/handler nil)]]])



(defn -main [& args]
  (let [port 3001
        vhosts-model
        (vhosts-model
          [{:scheme :http :host (format "localhost:%d" port)}
           routes])
        listener (yada/listener vhosts-model {:port port})]
    (infof "Started web-server on port %s" (:port listener))
    (println "TODO: proper logging!")
    (println "TODO: proper logging!")
    (println "http://localhost:3001/index")
    listener))
