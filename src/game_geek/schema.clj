(ns game-geek.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [clojure.edn :as edn]))

(defn resolve-game-by-id
  [games-map context args value]
  (let [{:keys [id]} args]
    (get games-map id)))

(defn resolve-board-game-designer
  [designers-map context args board-game]
  (->> board-game
       :designers
       (map designers-map)))

(defn resolve-designer-games
  [games-map context args designer]
  (let [{:keys [id]} designer]
    (->> games-map
         vals
         (filter #(-> %
                      :designers
                      (contains? id))))))

(defn entity-map
  [data k]
  (reduce #(assoc %1 (:id %2) %2)
          {}
          (get data k)))

(defn resolver-map
  []
  (let [data (-> (io/resource "data.edn")
                 slurp
                 edn/read-string)
        games-map (entity-map data :games)
        designers-map (entity-map data :designers)]
    (def -games games-map)
    (def -designers designers-map)
    {:query/game-by-id (partial resolve-game-by-id games-map)
     :BoardGame/designers (partial resolve-board-game-designer designers-map)
     :Designer/games (partial resolve-designer-games games-map)}))

(defn load-schema
  []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      (schema/compile)))

