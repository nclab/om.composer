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
package tech.metacontext.ocnhfa.antsomg.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.demo.x.Graph_X;
import tech.metacontext.ocnhfa.antsomg.demo.y.Graph_Y;
import tech.metacontext.ocnhfa.antsomg.demo.z.Graph_Z;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import tech.metacontext.ocnhfa.antsomg.impl.StandardParameters;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.*;
import tech.metacontext.ocnhfa.antsomg.model.AntsOMGSystem;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class DemoSystem implements AntsOMGSystem<DemoAnt> {

   int ant_population;
   Map<String, ? extends StandardGraph> graphs;
   List<DemoAnt> ants;

   public DemoSystem(int ant_population, long seed) {

      this.ant_population = ant_population;
      StandardParameters.initialization(seed);
   }

   @Override
   public void init_graphs() {

      this.graphs = Map.of(
              "x", new Graph_X(),
              "y", new Graph_Y(ALPHA, BETA * 2),
              "z", new Graph_Z(ALPHA * 2, BETA)
      );
      this.graphs.values().forEach(StandardGraph::init_graph);
   }

   @Override
   public void init_population() {

      this.ants = Stream.generate(()
              -> new DemoAnt(
                      getX().getStart(),
                      getY().getStart(),
                      getZ().getStart()))
              .limit(ant_population)
              .collect(Collectors.toList());
   }

   Graph_X getX() {

      return (Graph_X) this.graphs.get("x");
   }

   Graph_Y getY() {

      return (Graph_Y) this.graphs.get("y");
   }

   Graph_Z getZ() {

      return (Graph_Z) this.graphs.get("z");
   }

   @Override
   public void navigate() {

      this.ants.stream().forEach(ant -> {
         if (!ant.isCompleted()) {
            var trace = ant.getCurrentTrace();
            var x = getX().move(trace.getX().getSelected().getTo(),
                    PHEROMONE_DEPOSIT, EXPLORE_CHANCE);
            var y = getY().move(trace.getY().getSelected().getTo(),
                    PHEROMONE_DEPOSIT, EXPLORE_CHANCE);
            var z = getZ().move(trace.getZ().getSelected().getTo(),
                    PHEROMONE_DEPOSIT, EXPLORE_CHANCE);
            var new_trace = new DemoTrace(x, y, z);
            ant.setCurrentTrace(new_trace);
            if (new_trace.getDimension("x").equals(getX().getStart())
                    && new_trace.getDimension("y").equals(getY().getStart())
                    && new_trace.getDimension("z").equals(getZ().getStart())) {
               ant.addCurrentTraceToRoute();
               ant.setCompleted(true);
            }
         }
      });
      this.evaporate();
   }

   @Override
   public void evaporate() {

      Stream.of(getX().getEdges(), getY().getEdges(), getZ().getEdges())
              .flatMap(List::stream)
              .forEach(edge -> edge.evaporate(EVAPORATE_RATE));
   }

   @Override
   public boolean isAimAchieved() {

      return this.ants.stream().allMatch(DemoAnt::isCompleted);
   }

   @Override
   public Map<String, ? extends StandardGraph> getGraphs() {

      return this.graphs;
   }

   @Override
   public List<DemoAnt> getAnts() {

      return this.ants;
   }
}
