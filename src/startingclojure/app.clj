(ns startingclojure.app
	(:use [clojure.pprint :only (pprint)])
	(:use (compojure handler
		      [core :only (GET POST defroutes)]))
	(:require [ring.util.response :as response]
	          [net.cgrand.enlive-html :as en]
	          [ring.adapter.jetty :as jetty]))

#_ (require '(clojure [pprint :as pprint]))

(defonce counter (atom 10000))
(defonce urls (atom {}))

(defn shorten
	[url]
	(let [id (swap! counter inc)
	      id (Long/toString id 36)]
		(swap! urls assoc id url)
		id))

(en/deftemplate homepage
	(en/xml-resource "homepage.html")
	[request]
	[:#listing :li ] (en/clone-for [[id url] @urls]
		                 ;[:a ] (en/content (format "%s : %s" id url))
		                 ;[:a ] (en/set-attr :href (str \/ id))
		                 [:a ] (comp
			                       #_(fn [e] (update-in e [:content] concat [" *click here*"]))
			                       (en/content (format "%s : %s" id url))
			                       (en/set-attr :href (str \/ id)))
		                 ))

#_ (defn homepage ; replaced with enlive template en/deftemplate
	   [request]
	   #_{:status 200
	       :body (with-out-str
		             (pprint request))}
	   (str @urls))

(defn redirect
	[id]
	(response/redirect (@urls id)))

(defroutes app*
	(GET "/" request (homepage request))
	(POST "/shorten" request
		#_{:status 200  ; this will dump out the raw request object
		 :body (with-out-str (pprint request)) ; as a pretty printed
		 :headers {"Content-Type" "text/plain"}} ; plain text doc
		(let [id (shorten (-> request :params :url))]
			(response/redirect "/")))
	(GET "/:id" [id] (redirect id)))

(def app (compojure.handler/site app*)) ; bundle up middle-ware for param parsing et al

#_ (defn app ; original app handler
	   [request]
	   {:status 200
	    :body (with-out-str
		          (pprint request))})

#_ (defn run ; will start the original app handler as server
	   []
	   (defonce server (jetty/run-jetty #'app {:port 8080 :join? false}))
	   )

; 58 :25
; to delete an item: (swap! urls dissoc "7pu")