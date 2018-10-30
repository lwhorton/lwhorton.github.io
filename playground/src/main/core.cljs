(ns main.core
  (:require
    [goog.object :as g]))

(js-obj :a 1 :b 2)
(array 1 2 3)
(println (array :a 'b "c"))

(set! (. js/window -bar) #js {"a" 1 "b" {"c" 2 "d" 3}})

(set! (. js/window -bar) #js {:a 1 :b 2})
(def my-obj #js {"a" 1 "b" {"c" 2 "d" 3}})
(println my-obj)

(def my-arr #js ["a" :b 'c 4])
(set! (. js/window -foo) #js ["a" "b"])
(set! (. js/window -foo) #js ["a" :b 'c])

(set! (. js/window -baz) (clj->js {:a 1 'b 2 "c" {:d 3} }))
(js->clj (. js/window -baz) :keywordize-keys true)

(str "+" (name 'b))

(set! (. js/window -b) (clj->js {:a 1 'b 2 "c" {:d 3}} :keyword-fn (fn [x]
                                                                     (println x)
                                                                     (str "+" (name x)))))


(set! (. js/window -baz) (clj->js {:a 1 'b 2 "c" 3 ["d"] {:e "f"}}))
(js->clj (. js/window -baz))

(defn- break [x]
  (. x foo))

(defn ^:export -main []
  (println "hello world")
  ;(set! (. js/window -myObject) #js {foo: (fn [] (println "invoked"))})
  ((g/get js/window "foo"))
  (g/set js/window "bar" false)
  (println (g/get js/window "bar"))
  (. js/window foo))
