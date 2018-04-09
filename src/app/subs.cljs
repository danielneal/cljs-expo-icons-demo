(ns app.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-greeting
 (fn [db _]
   (:greeting db)))

(reg-sub
 :app-ready?
 (fn [db _]
   (:app-ready? db)))
