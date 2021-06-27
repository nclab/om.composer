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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.model.Ant;
import tech.metacontext.ocnhfa.composer.cf.model.constraints.MusicThreadRating;
import tech.metacontext.ocnhfa.composer.cf.model.devices.*;
import tech.metacontext.ocnhfa.composer.cf.model.enums.*;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchMove;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class MusicThread implements Ant<MusicTrace>, Comparable<MusicThread> {

  private CantusFirmus cf;
  private MusicTrace currentTrace;
  private List<MusicTrace> route;
  private boolean completed;
  /**
   * The chance the ant strays away from the path determined by pheromone
   */
  private double exploreChance;
  private double pheromoneDeposit;

  public MusicThread(EcclesiasticalMode ecclesiastical_mode, PitchNode finalis,
          MusicNode start, Logger logger) {

    ecclesiastical_mode = (ecclesiastical_mode == EcclesiasticalMode.RANDOM_MODE)
            ? EcclesiasticalMode.getRandomMode() : ecclesiastical_mode;
    this.cf = new CantusFirmus(ecclesiastical_mode, finalis);
    this.setCurrentTrace(new MusicTrace(start, this.cf.getFinalis()));
    this.route = new ArrayList<>();
    logger.log(Level.INFO, "MusicThread in {0}, starting at {1}, created.",
            new Object[]{ecclesiastical_mode.name(), this.cf.getFinalis().getPitch()});
  }

  public MusicThread(MusicNode start, Logger logger) {

    this(EcclesiasticalMode.getRandomMode(), null, start, logger);
  }

  public MusicThread(CantusFirmus cf) {

    this.cf = cf;
    this.completed = true;
  }

  @Override
  public void addCurrentTraceToRoute() {

    this.route.add(this.currentTrace);
  }

  public PitchPath lastPitchPath() {

    if (this.cf.length() < 2) {
      return null;
    }
    return new PitchPath(
            this.cf.getMelody().get(this.cf.length() - 2),
            this.cf.getMelody().getLast(), 0.0);
  }

  public int lastPitchDirection() {

    if (this.cf.length() < 2) {
      return 0;
    }
    return Pitch.diff(lastPitchPath());
  }

  /**
   * return the position of the last pitch in overall range.
   *
   * @return int: 0 if right in the middle, positive if higher than the middle,
   * vice versa.
   */
  public int lastPitchLevel() {

    return switch (this.cf.length()) {
      case 0, 1, 2 ->
        this.lastPitchDirection() / 2;
      default ->
        Pitch.diff(cf.getMiddle().getNode(), cf.getMelody().getLast());
    };
  }

  public int currentRange() {

    var summary = cf.getMelody().stream()
            .mapToInt(pn -> pn.getPitch().ordinal())
            .summaryStatistics();
    return summary.getMax() - summary.getMin() + 1;
  }

  public void addPitchMove(PitchMove pm) {

    this.cf.add(pm);
    this.setCurrentTrace(new MusicTrace(this.currentTrace.x, pm.getSelected().getTo()));
  }

  public void addByPitches(PitchNode... pitches) {

    var current = new AtomicReference<PitchNode>(this.cf.getMelody().getLast());
    Stream.of(pitches)
            .map(pitch -> {
              var path = new PitchPath(current.get(), pitch, 0.0);
              current.set(pitch);
              return new PitchMove(false, List.of(path), path, MusicThought.NULL);
            })
            .peek(cf::add)
            .map(PitchMove::getSelected)
            .map(PitchPath::getTo)
            .map(pn -> new MusicTrace(this.currentTrace.x, pn))
            .forEach(loc -> this.setCurrentTrace(loc));
  }

  public void addCadence(Cadence cadence) {

    this.addByPitches(cadence.getFormula().toArray(PitchNode[]::new));
    this.completed = true;
  }

  @Override
  public String toString() {

    return cf.toString()
            + String.format("(%.1f, %.1f, %.1f, %.1f -> %.1f)",
                    MusicThreadRating.range(this),
                    MusicThreadRating.dominantCount(this),
                    MusicThreadRating.length(this),
                    MusicThreadRating.leap(this),
                    MusicThreadRating.rate(this));
  }

  /*
     * Default getters and setters.
   */
  @Override
  public List<MusicTrace> getRoute() {

    return this.route;
  }

  public void setHistory(List<MusicTrace> history) {

    this.route = history;
  }

  public double getExploreRate() {

    return exploreChance;
  }

  public void setExploreChance(double exploreChance) {

    this.exploreChance = exploreChance;
  }

  public double getPheromoneDeposit() {

    return pheromoneDeposit;
  }

  public void setPheromoneDeposit(double pheromoneDeposit) {

    this.pheromoneDeposit = pheromoneDeposit;
  }

  @Override
  public MusicTrace getCurrentTrace() {

    return currentTrace;
  }

  @Override
  public void setCurrentTrace(MusicTrace currentLocation) {

    if (Objects.nonNull(this.currentTrace)) {
      this.addCurrentTraceToRoute();
    }
    this.currentTrace = currentLocation;
  }

  public boolean isCompleted() {

    return completed;
  }

  public void setCompleted(boolean completed) {

    this.completed = completed;
  }

  public CantusFirmus getCf() {

    return cf;
  }

  @Override
  public int compareTo(MusicThread o) {

    return Double.compare(MusicThreadRating.rate(o), MusicThreadRating.rate(this));
  }

  @Override
  public int hashCode() {

    return Objects.hashCode(this.cf.getMelody());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MusicThread other = (MusicThread) obj;
    return this.cf.getMelody().equals(other.cf.getMelody());
  }

}
