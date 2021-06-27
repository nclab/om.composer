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
package tech.metacontext.ocnhfa.composer.cf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.function.Predicate.not;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.impl.StandardParameters;
import tech.metacontext.ocnhfa.antsomg.model.*;
import tech.metacontext.ocnhfa.composer.cf.ex.*;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.*;
import tech.metacontext.ocnhfa.composer.cf.model.enums.*;
import static tech.metacontext.ocnhfa.composer.cf.model.enums.EcclesiasticalMode.RANDOM_MODE;
import tech.metacontext.ocnhfa.composer.cf.model.x.*;
import tech.metacontext.ocnhfa.composer.cf.model.y.*;

/**
 * Cantus Firmus Composer.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Composer implements AntsOMGSystem<MusicThread> {

  protected String id;
  protected Logger logger;

  private int thread_number = DEFAULT_COMPOSE_THREAD_NUMBER;
  private EcclesiasticalMode ecclesiastical_mode = RANDOM_MODE;
  private String preset_source;

  private Map<String, Graph> graphs;
  private List<MusicThread> music_threads;
  private int navigation_count; //navigation count
  private boolean toCadence;

  public double x_pheromone_deposit_amount = X_PHEROMONE_DEPOSIT_AMOUNT;
  public double y_pheromone_deposit_amount = Y_PHEROMONE_DEPOSIT_AMOUNT;
  public double x_explore_chance = X_EXPLORE_CHANCE;
  public double y_explore_chance = Y_EXPLORE_CHANCE;
  public double x_pheromone_evaporate_rate = X_PHEROMONE_EVAPORATE_RATE;
  public double y_pheromone_evaporate_rate = Y_PHEROMONE_EVAPORATE_RATE;

  public static synchronized Composer getInstance() {

    return new Composer();
  }

  public Composer setLogger(String id) {

    this.id = id;
    this.logger = Logger.getGlobal();
    this.logger.setUseParentHandlers(false);
    return this;
  }

  public void init() {

    System.out.println("Initializing Composer, id = " + this.id);

    this.logger.log(Level.INFO, "Initializing Composer, id = {0}", this.id);

    this.logger.log(Level.INFO, "Music thread number = {0}, ecclesiastical mode = {1}",
            new Object[]{this.thread_number, this.ecclesiastical_mode.name()});

    this.logger.log(Level.INFO, "Initializing MusicSpace...");
    init_graphs();

    this.logger.log(Level.INFO, "Initializing music threads...");
    init_population();
  }

  public void init(int thread_number, EcclesiasticalMode ecclesiastical_mode) {

    this.thread_number = thread_number;
    this.ecclesiastical_mode = ecclesiastical_mode;
    this.init();
  }

  public void init(Document doc, int thread_number) {

    this.thread_number = (thread_number > 0) ? thread_number : Integer.valueOf(
            doc.getRootElement().element("thread_number").getTextTrim());
    this.ecclesiastical_mode = EcclesiasticalMode.valueOf(
            doc.getRootElement().element("mode").getTextTrim());
    this.preset_source = doc.getRootElement().attributeValue("id");
    this.init();

    var x = doc.getRootElement().element("MusicSpace");
    x.elements("MusicPath").stream().forEach(e -> {
      var from = e.element("from").getTextTrim();
      var to = e.element("to").getTextTrim();
      var cost = Double.valueOf(e.element("cost").getTextTrim());
      var pheromoneTrail = Double.valueOf(e.element("pheromoneTrail").getTextTrim());
      var path = this.getX().queryByVertex(MusicThought.getNode(from)).stream()
              .filter(p -> Objects.equals(p.getTo(), MusicThought.getNode(to)))
              .findFirst().get();
      path.setCost(cost);
      path.setPheromoneTrail(pheromoneTrail);
    });
    var y = doc.getRootElement().element("PitchSpace");
    y.elements("PitchPath").stream().forEach(e -> {
      var from = e.element("from").getTextTrim().toUpperCase();
      var to = e.element("to").getTextTrim().toUpperCase();
      var cost = Double.valueOf(e.element("cost").getTextTrim());
      var pheromoneTrail = Double.valueOf(e.element("pheromoneTrail").getTextTrim());
      try {
        var path = this.getY().queryByVertex(Pitch.valueOf(from).getNode()).stream()
                .filter(p -> Objects.equals(p.getTo(), Pitch.valueOf(to).getNode()))
                .findFirst().get();
        path.setCost(cost);
        path.setPheromoneTrail(pheromoneTrail);
      } catch (Exception ex) {

      }
    });
  }

  public String asXML() {

    var doc = DocumentHelper.createDocument();
    var root = doc.addElement("CFComposer").addAttribute("id", this.id);
    if (Objects.nonNull(this.preset_source)) {
      root.addElement("preset_source").addText(this.preset_source);
    }
    root.addElement("thread_number").addText(String.valueOf(this.thread_number));
    root.addElement("mode").addText(this.ecclesiastical_mode.name());
    try {
      var x = DocumentHelper.parseText(this.getX().asXML()).getRootElement();
      var y = DocumentHelper.parseText(this.getY().asXML()).getRootElement();
      root.add(x);
      root.add(y);
    } catch (DocumentException ex) {
      Logger.getLogger(Composer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return doc.asXML();
  }

  @Override
  public void init_graphs() {

    this.graphs = new HashMap<>(Map.of(
            "x", new MusicSpace(),
            "y", new PitchSpace()));
    this.graphs.values().forEach(Graph::init_graph);
  }

  @Override
  public void init_population() {

    this.music_threads = new ArrayList<>();
    for (var i = 0; i < this.thread_number; i++) {
      var mt = new MusicThread(this.ecclesiastical_mode, null, this.getX().getStart(), this.logger);
      this.music_threads.add(mt);
    }
  }

  @Override
  public void navigate() {

    this.logger.log(Level.INFO, "*** navigating, navigation_count = {0}",
            navigation_count++);
    this.toCadence = navigation_count > CF_LENGTH_LOWER;

    this.music_threads.stream()
            .filter(not(MusicThread::isCompleted))
            .forEach(thread -> {
              boolean ok;
              do {
                ok = nav_y(thread, nav_x(thread));
              } while (!ok);
              this.logger.log(Level.INFO, thread.toString());
            });
    evaporate();
    System.out.print(".");
  }

  protected MusicThought nav_x(MusicThread thread)
          throws UnexpectedLocationException, UnexpectedMusicNodeException {

    if (!thread.getCurrentTrace().x.equals(this.getX().getStart())) {
      throw new UnexpectedLocationException(thread.getCurrentTrace());
    }
    var y = thread.getCurrentTrace().y;
    if (thread.getCf().length() >= 2 && thread.getCf().getMelody().getLast().getName().matches("[BF].")) {
      thread.setCurrentTrace(new MusicTrace(MusicThought.DIRECTIONAL, y));
      thread.setCurrentTrace(new MusicTrace(MusicThought.CONJUNCT, y));
      thread.setCurrentTrace(new MusicTrace(this.getX().getStart(), y));
      return MusicThought.Directional_Conjunct;
    }
    if (thread.getCf().length() >= 2 && thread.lastPitchPath().getInterval() > 3) {
      thread.setCurrentTrace(new MusicTrace(MusicThought.COMPLEMENTAL, y));
      thread.setCurrentTrace(new MusicTrace(MusicThought.SHORTTERM, y));
      thread.setCurrentTrace(new MusicTrace(this.getX().getStart(), y));
      return MusicThought.Complemental_ShortTerm;
    }

    var move1 = x_move(thread); //Start to Directional/Complemental
    var move2 = x_move(thread); //Directional/Complemental to Upward-Downward/ShortTerm-LongTerm
    MusicThought mt = MusicThought.getInstance(
            move1.getSelected().getTo(),
            move2.getSelected().getTo());
    if (Objects.isNull(mt)) {
      throw new UnexpectedMusicNodeException(
              move1.getSelected().getTo(),
              move2.getSelected().getTo());
    }
    var move3 = x_move(thread);
    if (!move3.getSelected().getTo().equals(this.getX().getStart())) {
      throw new UnexpectedLocationException(thread.getCurrentTrace());
    }
    thread.setCurrentTrace(new MusicTrace(move3, y));
    return mt;
  }

  private StandardMove<MusicPath> x_move(MusicThread thread) {

    var current_x = this.getX().move(thread.getCurrentTrace().x,
            x_pheromone_deposit_amount, x_explore_chance);
    thread.setCurrentTrace(new MusicTrace(current_x,
            thread.getCurrentTrace().y));
    return current_x;
  }

  protected boolean nav_y(MusicThread thread, MusicThought mt) {

    this.logger.log(Level.INFO, "nav_y invoked with MusicThought = {0}", mt.name());
    if (this.toCadence) {
      var cadence = thread.getCf().getEcclesiastical_Mode().getCadence(thread.getCurrentTrace().y);
      if (cadence != null && StandardParameters.getRandom().nextDouble() > y_explore_chance) {
        thread.addCadence(cadence);
        return true;
      }
    }
    var current_y = this.getY().move(thread.getCurrentTrace().y,
            thread.getCf().getDominant(), mt.getPredicate(thread),
            y_pheromone_deposit_amount, y_explore_chance);
    if (Objects.nonNull(current_y)) {
      current_y.setMt(mt);
      thread.addPitchMove(current_y);
      return true;
    }
    this.logger.log(Level.WARNING, "Current MusicThought leads to no possibilities.");
    return false;
  }

  @Override
  public void evaporate() {

    this.logger.log(Level.INFO, "evaporate...");
    this.getX().getEdges()
            .forEach(p -> p.evaporate(x_pheromone_evaporate_rate));
    this.getY().getEdges()
            .forEach(p -> p.evaporate(y_pheromone_evaporate_rate));
  }

  @Override
  public boolean isAimAchieved() {

    return music_threads.stream().allMatch(MusicThread::isCompleted);
  }

  /*
     * Default getters and setters.
   */
  @Override
  public List<MusicThread> getAnts() {

    return music_threads;
  }

  public void setAnts(List<MusicThread> mts) {

    this.music_threads = mts;
  }

  public boolean isToCadence() {

    return toCadence;
  }

  public void setToCadence(boolean toCadence) {

    this.toCadence = toCadence;
  }

  @Override
  public Map<String, Graph> getGraphs() {

    return this.graphs;
  }

  public MusicSpace getX() {

    return (MusicSpace) getGraphs().get("x");
  }

  public PitchSpace getY() {

    return (PitchSpace) getGraphs().get("y");
  }

  public List<MusicThread> getMusicThreads() {

    return music_threads;
  }

  public void setMusicThreads(List<MusicThread> musicThreads) {

    this.music_threads = musicThreads;
  }

  public int getNavigation_count() {

    return navigation_count;
  }

  public void setNavigation_count(int navigation_count) {

    this.navigation_count = navigation_count;
  }

  public int getThread_number() {

    return thread_number;
  }

  public void setThread_number(int thread_number) {

    this.thread_number = thread_number;
  }

  public String getId() {

    return this.id;
  }

  public void setFraction_mode(FractionMode fraction_mode) {

    this.getX().setFraction_mode(fraction_mode);
    this.getY().setFraction_mode(fraction_mode);
  }

  public EcclesiasticalMode getEcclesiastical_mode() {

    return ecclesiastical_mode;
  }

  public void setEcclesiastical_mode(EcclesiasticalMode ecclesiastical_mode) {

    this.ecclesiastical_mode = ecclesiastical_mode;
  }

  public String getPreset_source() {

    return preset_source;
  }

  public void setPreset_source(String preset_source) {

    this.preset_source = preset_source;
  }

  public Logger getLogger() {

    return this.logger;
  }
}
