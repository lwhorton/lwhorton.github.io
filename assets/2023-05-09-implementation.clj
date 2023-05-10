(ns example)

; what does a workflow look like? a map, of course!
#:workflow{:id "" ; uuid uniquely identifying a workflow
           :ident :CA-district-19A ; human identifiable alias for a workflow
           :version :v2023-03-05 ; identifies a stamped/minted version of an immutable workflow (enables changes to workflows over time, record keeping)
           :current-step "" ; uuid pointer to the current step a substitute is currently working on, or waiting for
           :step-order [ 'uuid1 'uuid3 'uuid2 'uuid4 ] ; an ordered vector describing the strict series of steps in an onboarding flow
           :complete? false ; boolean indicating if the assigned substitute-id has completed the onboarding
           :substitute-id "" ; uuid identifying the substitute teacher assigned to complete the onboarding instance
           :started? false ; boolean indicating if the assigned substitute-id has begun the onboarding
           :started-at "2023-05-03 12:00:00Z" ; timestamp marking the moment our system learned the substitute completed the onboarding
           :completed-at "2023-05-05 12:00:00Z" ; timestamp marking the moment our system learned the substitute began onboarding
           }

; what does a step look like?
#:step{:id ""	; uuid uniquely identifying an instance of a step
       :action :proof-of-insurance	; the activity a substitute must complete	chekr-validation, covid-vaccination, livescan, proof-of-insurance
       :complete? false	; boolean indicating if the assigned substitute-id has satisfied the step
       :assignee ""	; uuid of the substitute assigned to complete this step
       :version	"v2020-01-04" ; identifies a stamped/minted version of an immutable step (enables changes to steps over time, record keeping)	:artifact-ref "s3://domain.artifacts...credentials/1234-proof-of-vaccination" ; a link to the digital proof (receipt, photo, credential URI, etc) that satisfied the requirements of this step	:optionality :optional	; identifies if a given step is required, optional, undetermined, etc. to progress to the next step in an onboarding flow
       :deadline "2023-05-30"	; a timestamp indicating that a given step must be completed before a particular moment in time
       }

; what capabilities might a consumer want?
(current-step workflow)
(next-step workflow)
(progress-to-next-step workflow)

; how do we handle inbound/outbound side-effects for a step's actions?
(defmulti emit-step-side-effect :action)
(defmethod emit-step-side-effect [:chekr-validation] [_]  (email.outbound/send { ,,, })

(defmulti receive-step-side-effect :action)
(defmethod receive-step-side-effect [:chekr-validation] [validation]  (-> validation (authenticate) (confirm) (store))
(defmethod receive-step-side-effect [:proof-of-insurance] [receipt]  (-> receipt (authenticate) (receive) (record))

