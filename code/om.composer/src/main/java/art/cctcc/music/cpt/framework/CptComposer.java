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
package art.cctcc.music.cpt.framework;

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.ex.ImmatureCptCfPitchSpaceException;
import art.cctcc.music.cpt.graphs.x.CptMusicMove;
import art.cctcc.music.cpt.graphs.x.CptMusicSpace;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchMove;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchSpaceChromatic;
import art.cctcc.music.cpt.graphs.y_cpt_cf.CptCfPitchSpace;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.cpt.model.enums.CptTask;
import static art.cctcc.music.cpt.model.enums.CptTask.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.model.AntsOMGSystem;
import tech.metacontext.ocnhfa.antsomg.model.Graph;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedMusicNodeException;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;

/**
 * Composer implementation for First Species counterpoint by extending
 * AntsOMGSystem, to generate a population of counterpoint threads.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptComposer implements AntsOMGSystem<CptThread> {

  private String id;
  private CptCantusFirmus cf;
  private boolean treble;

  private CptTask task;
  private int population = CPT_DEVELOPING_POPULATION;
  private double x_pheromone_deposit = X_PHEROMONE_DEPOSIT_AMOUNT;
  private double y_pheromone_deposit = Y_PHEROMONE_DEPOSIT_AMOUNT;
  private double x_pheromone_evaporate_rate = X_PHEROMONE_EVAPORATE_RATE;
  private double y_pheromone_evaporate_rate = Y_PHEROMONE_EVAPORATE_RATE;

  private boolean parallel;
  private boolean chromatic;

  private Map<String, Graph> graphs;
  private List<CptThread> threads;

  public static CptComposer getInstance(String id, CptCantusFirmus cf, boolean isTreble) {

    return new CptComposer(id, cf, isTreble);
  }

  private CptComposer(String id, CptCantusFirmus cf, boolean isTreble) {

    this.id = id;
    this.cf = cf;
    this.treble = isTreble;
    this.setTask(DEVELOP_PRIMARY);
  }

  @Override
  public void init_graphs() {

    var y = new CptCfPitchSpace(this.cf, this.treble);
    if (chromatic) {
      y.setYCpt(CptPitchSpaceChromatic.getInstance());
      CptEvaluation.type = CptEvaluation.EvalType.chromatic;
    }
    this.graphs = new HashMap<>(Map.of(
            "x", new CptMusicSpace(),
            "y", y));
    try {
      this.graphs.values().forEach(Graph::init_graph);
    } catch (ImmatureCptCfPitchSpaceException ex) {
      System.out.println(ex);
    }
  }

  @Override
  public void init_population() {

    this.threads = Stream.generate(this::generate)
            .limit(population)
            .collect(Collectors.toList());
  }

  public CptMusicSpace getX() {

    return (CptMusicSpace) this.graphs.get("x");
  }

  public void setX(CptMusicSpace x) {

    this.graphs.put("x", x);
  }

  public CptCfPitchSpace getY() {

    return (CptCfPitchSpace) this.graphs.get("y");
  }

  public CptThread generate() {

    return new CptThread(this.cf, this.getY().getStart());
  }

  int counter, section;

  @Override
  public void navigate() {

    counter = 0;
    section = this.threads.size() * (cf.length() - 1) / 20;
    StreamSupport.stream(this.threads.spliterator(), parallel)
            .peek(this::developThread)
            .forEach(t -> {
              while (counter >= section) {
                counter -= section;
                System.out.print("=");
              }
            });
    Collections.sort(this.threads);
  }

  public void developThread(CptThread thread) {

    IntStream.range(1, cf.length())
            .forEach(i -> {
              var x_move = nav_x(thread);
              var y_move = nav_y(i, thread, x_move);
              thread.setCurrentTrace(new CptTrace(x_move, y_move));
            });
    assert thread.getCpt().length() == cf.length();
    thread.setCompleted(true);
    counter += (cf.length() - 1);
    if (this.task != COMPOSE) {
      this.evaporate();
    }
  }

  public CptMusicMove nav_x(CptThread thread) {

    var cpt = thread.getCpt();
    CptMusicMove x_move;
    if (cpt.length() >= 2 && cpt.getMelody().getLast().getName().matches("[BF].")) {
      x_move = new CptMusicMove(MusicThought.DIRECTIONAL, MusicThought.CONJUNCT);
    } else if (cpt.length() >= 2 && thread.lastPitchPath().absDiff() > 5) {
      x_move = new CptMusicMove(MusicThought.COMPLEMENTAL, MusicThought.SHORTTERM);
    } else {
      var move0 = this.getX().getMove(this.getX().getStart(), X_EXPLORE_CHANCE);
      var move1 = this.getX().getMove(move0.getSelected().getTo(), X_EXPLORE_CHANCE);
      x_move = new CptMusicMove(move0, move1);
    }
    if (Objects.isNull(x_move.getMusicThought())) {
      throw new UnexpectedMusicNodeException(
              x_move.getMoves()[0].getSelected().getTo(),
              x_move.getMoves()[1].getSelected().getTo());
    }
    this.getX().move(x_move, x_pheromone_deposit);
    return x_move;
  }

  public CptPitchMove nav_y(int locus, CptThread thread, CptMusicMove x_move) {

    CptPitchMove y_move;
    var current = thread.getCurrentTrace().getY().getSelected().getTo();
    do {
      y_move = this.getY().getMove(
              locus,
              current,
              Y_EXPLORE_CHANCE);
    } while (this.getY().queryByVertex(locus, current).stream()
            .filter(x_move.getPredicate(thread)).count() > 0
            && !x_move.getPredicate(thread).test(y_move.getSelected()));
    this.getY().move(y_move, y_pheromone_deposit);
    return y_move;
  }

  @Override
  public void evaporate() {

    if (this.task != DEVELOP_SECONDARY) {
      this.getX().getEdges()
              .forEach(p -> p.evaporate(x_pheromone_evaporate_rate));
    }
    var loci = this.getY().getLoci();
    IntStream.range(0, loci.size()).boxed()
            .flatMap(i -> loci.get(i).stream())
            .forEach(p -> p.evaporate(y_pheromone_evaporate_rate));
  }

  @Override
  public boolean isAimAchieved() {

    return this.threads.stream().allMatch(CptThread::isCompleted);
  }

  public void insert(CptThread... inserted_threads) {

    var new_threads = Stream.of(List.of(inserted_threads), this.threads)
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .limit(population)
            .collect(Collectors.toList());

    this.threads = new_threads;
  }

  public String asXML() {

    var doc = DocumentHelper.createDocument();
    var root = doc.addElement("CptComposer").addAttribute("id", this.id);
    root.addElement("thread_number")
            .addText(String.valueOf(this.threads.size()));
    root.addElement("mode")
            .addText(this.cf.getMode().name());
    root.addElement("cantus_firmus")
            .addText(this.cf.getId());
    root.addElement("cantus_firmus_melody")
            .addText(this.cf.getMelody().toString());
    root.addElement("counterpoint_place")
            .addText(this.treble ? "treble" : "bass");
    try {
      var x = DocumentHelper.parseText(this.getX().asXML()).getRootElement();
      var y = DocumentHelper.parseText(this.getY().asXML()).getRootElement();
      root.add(x);
      root.add(y);
    } catch (DocumentException ex) {
      Logger.getLogger(CptComposer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return doc.asXML();
  }

  @Override
  public Map<String, Graph> getGraphs() {

    return this.graphs;
  }

  public void setGraphs(Map<String, Graph> graphs) {

    this.graphs = graphs;
  }

  @Override
  public List<CptThread> getAnts() {

    return this.threads;
  }

  public void setAnts(List<CptThread> threads) {

    this.threads = threads;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public CptCantusFirmus getCf() {

    return cf;
  }

  public void setCf(CptCantusFirmus cf) {

    this.cf = cf;
  }

  public boolean isTreble() {

    return treble;
  }

  public void setAbove(boolean isTreble) {

    this.treble = isTreble;
  }

  public CptTask getTask() {

    return task;
  }

  public int getPopulation() {

    return population;
  }

  public void setTask(CptTask task) {

    this.task = task;
    switch (task) {
      case DEVELOP_PRIMARY -> {
        population = CPT_DEVELOPING_POPULATION;
        x_pheromone_deposit = X_PHEROMONE_DEPOSIT_AMOUNT;
        y_pheromone_deposit = Y_PHEROMONE_DEPOSIT_AMOUNT;
        x_pheromone_evaporate_rate = X_PHEROMONE_EVAPORATE_RATE;
        y_pheromone_evaporate_rate = Y_PHEROMONE_EVAPORATE_RATE;
      }
      case DEVELOP_SECONDARY -> {
        population = CPT_DEVELOPING_POPULATION;
        x_pheromone_deposit = 0.0;
        y_pheromone_deposit = Y_PHEROMONE_DEPOSIT_AMOUNT;
        x_pheromone_evaporate_rate = 0.0;
        y_pheromone_evaporate_rate = Y_PHEROMONE_EVAPORATE_RATE;
      }
      case COMPOSE -> {
        population = CPT_COMPOSING_POPULATION;
        x_pheromone_deposit = 0.0;
        y_pheromone_deposit = 0.0;
        x_pheromone_evaporate_rate = 0.0;
        y_pheromone_evaporate_rate = 0.0;
      }
    }
  }

  public void setPopulation(int population) {

    this.population = population;
  }

  public void setParallel(boolean parallel) {

    this.parallel = parallel;
  }

  public void setChromatic(boolean chromatic) {

    this.chromatic = chromatic;
  }

  public double getAverageEval() {

    return this.threads.stream()
            .map(thread -> CptEvaluation.getInstance(thread))
            .mapToDouble(CptEvaluation::get)
            .average().getAsDouble();
  }

  @Override
  public String toString() {

    var count = threads.stream()
            .collect(Collectors.groupingBy(CptThread::isCompleted, Collectors.counting()));
    return String.format("""
           CptComposer{
           \tid = %s
           \tcf = %s
           \ttreble = %b
           \ttask = %s
           \tpopulation = %d
           \tparallel = %b
           \tx_pheromone_deposit = %.2f
           \ty_pheromone_deposit = %.2f
           \tx_pheromone_evaporate_rate = %.2f 
           \ty_pheromone_evaporate_rate = %.2f 
           \tgraph x: %s
           \tgraph y: %s
           \tthreads: completed = %d, incompleted = %d
           }""", id, cf, treble, task, population, parallel,
            x_pheromone_deposit, y_pheromone_deposit,
            x_pheromone_evaporate_rate, y_pheromone_evaporate_rate,
            this.getX() == null ? "not initialized." : "initialized, " + (this.getX().isBlank() ? "blank." : "not blank."),
            this.getY() == null ? "not initialized." : "initialized, " + (this.getY().isBlank() ? "blank." : "not blank."),
            count.getOrDefault(Boolean.TRUE, 0L), count.getOrDefault(Boolean.FALSE, 0L));
  }
}
