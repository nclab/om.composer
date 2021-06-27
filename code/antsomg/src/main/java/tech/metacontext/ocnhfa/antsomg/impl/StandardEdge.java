/*
 * Copyright 2021 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.metacontext.ocnhfa.antsomg.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tech.metacontext.ocnhfa.antsomg.model.Edge;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <V>
 */
public class StandardEdge<V extends StandardVertex> implements Edge<V> {

   private double pheromoneTrail;
   private double cost;
   private V from, to;

   public StandardEdge(V from, V to, double cost) {

      this.from = from;
      this.to = to;
      this.cost = cost;
   }

   public <E extends StandardEdge> E getReverse() {

      return getReverse(this.cost);
   }

   public <E extends StandardEdge> E getReverse(double cost) {

      try {
         return (E) this.getClass().getDeclaredConstructor(to.getClass(), from.getClass(), Double.class)
                 .newInstance(to, from, cost);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
         Logger.getLogger(StandardEdge.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{"
              + "from=" + from + ", to=" + to
              + (cost > 0 ? ", cost=" + cost : "")
              + (pheromoneTrail > 0 ? ", pheromoneTrail=" + pheromoneTrail : "")
              + '}';
   }

   @Override
   public void addPheromoneDeposit(double pheromoneDeposit) {

      this.pheromoneTrail += pheromoneDeposit;
   }

   @Override
   public double getPheromoneTrail() {

      return this.pheromoneTrail;
   }

   public void setPheromoneTrail(double pheromoneTrail) {

      this.pheromoneTrail = pheromoneTrail;
   }

   @Override
   public double getCost() {

      return this.cost;
   }

   public void setCost(double cost) {
      this.cost = cost;
   }

   @Override
   public V getFrom() {

      return this.from;
   }

   @Override
   public void evaporate(double rate) {

      this.pheromoneTrail *= (1.0 - rate);
   }

   @Override
   public void setFrom(V from) {

      this.from = from;
   }

   @Override
   public void setTo(V to) {

      this.to = to;
   }

   @Override
   public V getTo() {

      return this.to;
   }

}
