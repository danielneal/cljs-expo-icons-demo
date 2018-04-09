(ns app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.core.async :refer [<!]]
            [app.handlers]
            [app.subs]))

(def ReactNative (js/require "react-native"))
(def Expo (js/require "expo"))
;;(def Font (.-Font Expo))
(def Font (aget Expo "Font"))
(def Asset (aget Expo "Asset"))

;;(def VectorIcons (js/require "@expo/vector-icons"))
;;(def FontAwesome (.-FontAwesome VectorIcons))
;;(def <fontawesome-icon> (r/adapt-react-class FontAwesome))
;;(def MaterialIcons (.-MaterialIcons VectorIcons))
;;(def <material-icon> (r/adapt-react-class MaterialIcons))

(def FontAwesome (js/require "@expo/vector-icons/FontAwesome"))
(def <fontawesome-icon> (r/adapt-react-class (aget FontAwesome "default")))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))

;; Assets

(defn- cache-images
  [images]
  (for [image images]
    (when image
      (-> (.fromModule Asset image)
          (.downloadAsync)))))

(defn- cache-fonts
  [fonts]
  (for [font fonts]
    (when font
      (.loadAsync Font font))))

(defn- cast-as-array
  [coll]
  (if (or (array? coll)
          (not (reduceable? coll)))
    coll
    (into-array coll)))

(defn all
  [coll]
  (.all js/Promise (cast-as-array coll)))

(defn cache-assets
  [images fonts cb]
  (->
   (all (concat (cache-fonts (clj->js fonts)) (cache-images (clj->js images))))
   (.then (fn [resp]
            (if cb (cb))))
   (.catch (fn [err]
             (println "Loading assets failed: " (aget err "message"))))))

;; Styles

(def icon-style {:flex-direction "row"
                 :align-items "center"
                 :padding-left 10
                 :padding-right 10
                 :height 50
                 :background-color "rgba(255,255,255,0.90)"
                 :border-bottom-width 0.5
                 :border-color "#cccccc"})

;; App

(defn alert [title]
  (.alert Alert title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        app-ready? (subscribe [:app-ready?])]

    (cache-assets []
                  [{"pacifico" (js/require "./assets/fonts/Pacifico.ttf")}]
                  #(dispatch [:set-app-ready? true]))

    (fn []
      (cond
        (false? @app-ready?)
        (do
          (println "Hey! App loading...")
          [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
          [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} "Loading..."]])

        :else
        (r/create-class
         {:reagent-render
          (fn []
            [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
             [image {:source (js/require "./assets/images/cljs.png")
                     :style {:width 200
                             :height 200}}]

             [text {:style {:font-size 30
                            :font-weight "100"
                            :margin-bottom 20
                            :text-align "center"}}
              @greeting]

             [text {:style {:font-size 25
                            :font-family "pacifico"
                            :padding-top 8
                            :padding-bottom 8}}
              "hey pacifico"]

             [text {:style {:font-size 25
                            :font-family "serif"
                            :padding-top 8
                            :padding-bottom 8}}
              "hey serif"]

             [<fontawesome-icon> {:name "camera" :size 30}]

             ;;[<material-icon> {:name "camera"}]

             [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                   :on-press #(alert "HELLO!")}
              [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]])})))))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
