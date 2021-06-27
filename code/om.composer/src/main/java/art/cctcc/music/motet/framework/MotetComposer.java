/*
 * Copyright 2020 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music.motet.framework;

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.graphs.x.CptMusicSpace;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.motet.graphs.SectionGraph;
import art.cctcc.music.motet.model.Motet;
import art.cctcc.music.motet.model.enums.MotetComposerType;
import static art.cctcc.music.motet.model.enums.MotetComposerType.*;
import static art.cctcc.music.motet.model.enums.SectionType.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.EVAPORATE_RATE;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.EXPLORE_CHANCE;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.PHEROMONE_DEPOSIT;
import tech.metacontext.ocnhfa.antsomg.model.AntsOMGSystem;
import tech.metacontext.ocnhfa.antsomg.model.Graph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MotetComposer implements AntsOMGSystem<SectionPlanner> {

  private final String id;
  private SectionGraph graph;
  private List<SectionPlanner> threads;

  private MotetComposerType type;
  private int population;
  private double pheromone_deposit;

  private final Map<String, CptMusicSpace> x;

  private List<Motet> motets;

  private boolean parallel;

  private boolean chromatic;

  public MotetComposer(String id) {

    this.id = id;
    this.x = new HashMap<>();
    this.setType(DEVELOP);
  }

  public MotetComposer(String id, CptMusicSpace preset_x) {

    this(id);
    this.x.put("PRESET", preset_x);
  }

  public void plan(int motet_no) {

    this.setType(this.type == TEST ? TEST : PLAN);

    var schemes = Stream.generate(this::_getPlan)
            .filter(MotetComposer::_qualifiedPlan)
            .map(SectionPlanner::getSections)
            .distinct()
            .limit(motet_no)
            .sorted(Comparator.comparing(List::size))
            .collect(Collectors.toList());

    this.motets = IntStream.range(0, motet_no)
            .mapToObj(i -> new Motet(i + 1, schemes.get(i)))
            .collect(Collectors.toList());
  }

  public void compose(List<CptCantusFirmus> cf_list) {

    StreamSupport.stream(this.motets.spliterator(), parallel)
            .forEach(motet -> {
              motet.setGraph_x(x);
              motet.select_cf(cf_list);
              motet.setTest(this.type == TEST);
              motet.setParallel(parallel);
              motet.setChromatic(chromatic);
              motet.compose();
            });
  }

  private SectionPlanner _getPlan() {

    this.init_population();
    do {
      this.navigate();
    } while (!this.isAimAchieved());
    return this.getAnts().get(0);
  }

  private static boolean _qualifiedPlan(SectionPlanner plan) {

    var sections = plan.getSections().stream()
            .map(node -> node.toString().substring(0, 1))
            .collect(Collectors.joining());
    if (sections.length() > MAX_SECTION_NUMBER || sections.length() < MIN_SECTION_NUMBER) {
      return false;
    }
    if (sections.replaceAll("[TB]", "").length() < MIN_CF_NUMBER) {
      return false;
    }
    return Arrays.stream(sections.split("\\*")).filter(not(String::isEmpty))
            .allMatch(s -> s.matches(".*(?=.*T)(?=.*B).*"));
  }

  @Override
  public void init_graphs() {

    graph = new SectionGraph();
    graph.init_graph();
  }

  @Override
  public void init_population() {

    threads = Stream.generate(SectionPlanner::new)
            .limit(population)
            .collect(Collectors.toList());
  }

  @Override
  public void navigate() {

    this.threads.stream()
            .filter(not(SectionPlanner::isCompleted))
            .forEach(thread -> {
              var trace = thread.getCurrentTrace();
              var move = graph.move(trace.getMove().getSelected().getTo(),
                      pheromone_deposit, EXPLORE_CHANCE);
              thread.setCurrentTrace(new SectionTrace(move));
              if (FINISH.node.equals(move.getSelected().getTo())) {
                thread.setCompleted(true);
              }
            });
    this.evaporate();
  }

  @Override
  public void evaporate() {

    if (type == DEVELOP) {
      this.graph.getEdges()
              .forEach(edge -> edge.evaporate(EVAPORATE_RATE));
    }
  }

  @Override
  public boolean isAimAchieved() {

    return this.threads.stream().allMatch(SectionPlanner::isCompleted);
  }

  @Override
  public Map<String, ? extends Graph> getGraphs() {

    return Map.of("", this.graph);
  }

  @Override
  public List<SectionPlanner> getAnts() {

    return this.threads;
  }

  public String getId() {

    return id;
  }

  public SectionGraph getGraph() {

    return graph;
  }

  public List<SectionPlanner> getThreads() {

    return threads;
  }

  public final void setType(MotetComposerType type) {

    this.type = type;
    switch (type) {
      case DEVELOP -> {
        population = PLANNER_DEVELOPING_POPULATION;
        pheromone_deposit = PHEROMONE_DEPOSIT;
      }
      case PLAN -> {
        population = PLANNER_PLANNING_POPULATION;
        pheromone_deposit = 0.0;
      }
      case TEST -> {
        population = PLANNER_TESTING_POPULATION;
        pheromone_deposit = PHEROMONE_DEPOSIT;
      }
    }
  }

  public List<Motet> getMotets() {

    return motets;
  }

  public Map<String, CptMusicSpace> getX() {

    return x;
  }

  public void setParallel(boolean parallel) {

    this.parallel = parallel;
  }

  public void setChromatic(boolean chromatic) {

    this.chromatic = chromatic;
  }

  @Override
  public String toString() {

    return "MotetComposer [" + id + "]";
  }

}
