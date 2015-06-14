(ns lambdajam.jobs.test-1-1
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :refer [resource]]
            [com.stuartsierra.component :as component]
            [lambdajam.launcher.dev-system :refer [onyx-dev-env]]
            [lambdajam.challenge-1-1 :as c]
            [lambdajam.workshop-utils :as u]
            [onyx.api]
            [user]))

;; This challenge builds on the previous challenge - you'll implement
;; your first workflow. Below is a pictorial description of what
;; the workflow should look like:
;;
;;    read-segments
;;         |
;;         v
;;       cube-n
;;         |
;;         v
;;      add-ten
;;         |
;;         v
;;    multiply-by-5
;;         |
;;         v
;;    write-segments
;;
;; Open the src file for this challenge and fill in the workflow.
;; The workflow is left as an undefined var. Add the appropriate data
;; structure.
;;
;; Look for the "<<< BEGIN FILL ME IN >>>" and "<<< END FILL ME IN >>>"
;; comments to start your work.
;;
;; Try it with:
;;
;; `lein test lambdajam.jobs.test-1-0`


(def input (mapv (fn [n] {:n n}) (range 10)))

(def expected-output
  [{:n 50}
   {:n 55}
   {:n 90}
   {:n 185}
   {:n 370}
   {:n 675}
   {:n 1130}
   {:n 1765}
   {:n 2610}
   {:n 3695}])

(deftest test-level-1-challenge-1
  (try
    (let [catalog (c/build-catalog)
          dev-cfg (-> "dev-peer-config.edn" resource slurp read-string)
          lifecycles (c/build-lifecycles)]
      (user/go (u/n-peers catalog c/workflow))
      (u/bind-inputs! lifecycles {:read-segments input})
      (let [peer-config (assoc dev-cfg :onyx/id (:onyx-id user/system))
            job {:workflow c/workflow
                 :catalog catalog
                 :lifecycles lifecycles
                 :task-scheduler :onyx.task-scheduler/balanced}]
        (onyx.api/submit-job peer-config job)
        (let [[results] (u/collect-outputs! lifecycles [:write-segments])]
          (u/segments-equal? expected-output results))))
    (catch InterruptedException e
      (Thread/interrupted))
    (finally
     (user/stop))))
