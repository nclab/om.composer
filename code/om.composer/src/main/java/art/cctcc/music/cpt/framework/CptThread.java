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

import art.cctcc.music.cpt.ex.InvalidUseOfCptThreadForECException;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchMove;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.cpt.model.CptCounterpoint;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import tech.metacontext.ocnhfa.antsomg.model.Ant;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptThread implements Ant<CptTrace>, Comparable {

  private final CptCounterpoint cpt;
  private final LinkedList<CptTrace> route;
  private CptTrace currentTrace;
  private boolean completed;
  /**
   * The chance the ant strays away from the path determined by pheromone
   */
  private double exploreChance;
  private double pheromoneDeposit;

  /**
   * Constructor for generation from AntsOMG framework.
   *
   * @param cf
   * @param entry
   */
  public CptThread(CptCantusFirmus cf, CptPitchNode entry) {

    this.cpt = new CptCounterpoint(cf);
    this.cpt.addNote(entry);
    this.currentTrace = new CptTrace(null, new CptPitchMove(entry));
    this.route = new LinkedList<>();
  }

  /**
   * Constructor for evolutionary computation operation.
   *
   * @param cf
   * @param melody
   */
  public CptThread(CptCantusFirmus cf, List<CptPitchNode> melody) {

    this.route = null;
    this.cpt = new CptCounterpoint(cf);
    melody.stream().forEach(this.cpt::addNote);
  }

  @Override
  public void setCurrentTrace(CptTrace trace) {

    this.getCpt().addNote(trace.getY().getSelected().getTo());
    if (Objects.nonNull(this.currentTrace)) {
      this.addCurrentTraceToRoute();
    }
    this.currentTrace = trace;
  }

  @Override
  public void addCurrentTraceToRoute() {

    if (this.route == null) {
      throw new InvalidUseOfCptThreadForECException();
    }
    this.route.add(this.currentTrace);
  }

  @Override
  public CptTrace getCurrentTrace() {

    if (this.route == null) {
      throw new InvalidUseOfCptThreadForECException();
    }
    return this.currentTrace;
  }

  public CptPitchPath lastPitchPath() {

    return this.route == null
            ? CptPitchPath.of(this.cpt.getNote(this.cpt.length() - 2), this.cpt.getMelody().getLast())
            : this.currentTrace.getY().getSelected();
  }

  public int lastPitchDirection() {

    return (this.cpt.length() > 1 || this.route != null && this.route.size() > 0)
            ? CptPitch.diff(this.lastPitchPath()) : 0;
  }

  public int lastPitchLevel() {

    return CptPitch.diff(
            getCpt().getMiddle().getNode(),
            getCpt().getMelody().getLast());
  }

  @Override
  public int hashCode() {

    int hash = 5;
    hash = 29 * hash + Objects.hashCode(this.cpt.getMelody());
    hash = 29 * hash + Objects.hashCode(this.cpt.getCf().getMelody());
    return hash;
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
    final CptThread other = (CptThread) obj;
    return Objects.deepEquals(this.cpt.getMelody(), other.cpt.getMelody())
            && Objects.deepEquals(this.cpt.getCf().getMelody(), other.cpt.getCf().getMelody());
  }

  @Override
  public int compareTo(Object obj) {

    final CptThread other = (CptThread) obj;
    var compare = Double.compare(CptEvaluation.getInstance(other).get(),
            CptEvaluation.getInstance(this).get());
    return compare;
    /*
       != 0 ? compare : other.toString().compareTo(this.toString());
     */
  }

  @Override
  public String toString() {
    return "CptThread{" + "cpt=" + cpt + '}';
  }

  @Override
  public List<CptTrace> getRoute() {

    if (this.route == null) {
      throw new InvalidUseOfCptThreadForECException();
    }
    return this.route;
  }

  public CptCounterpoint getCpt() {

    return cpt;
  }

  public boolean isCompleted() {

    return completed;
  }

  public void setCompleted(boolean completed) {

    this.completed = completed;
  }

  public double getExploreChance() {

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
}
