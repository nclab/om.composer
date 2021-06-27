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
package tech.metacontext.ocnhfa.composer.cf.model.devices;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.composer.cf.model.enums.EcclesiasticalMode;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchMove;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class CantusFirmus {

  private EcclesiasticalMode ecclesiastical_mode;
  private PitchNode finalis, dominant;
  private LinkedList<PitchMove> pitch_route;

  public CantusFirmus(EcclesiasticalMode ecclesiastical_mode, PitchNode finalis) {

    this.ecclesiastical_mode = ecclesiastical_mode;
    var fi_do = (finalis == null) ? ecclesiastical_mode.getRandomFinalis()
            : ecclesiastical_mode.getDominant(finalis);
    this.finalis = fi_do.getKey();
    this.dominant = fi_do.getValue();
    this.pitch_route = new LinkedList<>();
    this.add(new PitchMove(false, List.of(), 
            new PitchPath(null, this.finalis, 0.0), MusicThought.NULL));
  }

  public CantusFirmus(EcclesiasticalMode ecclesiastical_mode,
          Collection<PitchMove> history) {

    this.ecclesiastical_mode = ecclesiastical_mode;
    this.pitch_route = new LinkedList<>(history);
    this.finalis = this.getMelody().getFirst();
    this.dominant = this.ecclesiastical_mode.getDominant(this.finalis)
            .getValue();
  }

  public void add(PitchMove pitch_history) {

    this.pitch_route.add(pitch_history);
  }

  public void add(PitchNode pitch_node) {

    var from = this.pitch_route.isEmpty()
            ? null
            : this.pitch_route.getLast().getSelected().getTo();
    var edge = new PitchPath(from, pitch_node, 0.0);
    this.add(new PitchMove(false, List.of(edge), edge, MusicThought.NULL));
  }

  public int length() {

    return pitch_route.size();
  }

  public Pitch getMiddle() {

    var summary = this.getMelody().stream()
            .mapToInt(pn -> pn.getPitch().ordinal())
            .summaryStatistics();
    var min = summary.getMin();
    var max = summary.getMax();
    return Pitch.values()[min + (max - min) / 2];
  }

  @Override
  public String toString() {

    return this.getMelody().toString()
            .replaceAll(this.dominant.getName(), this.dominant.getName() + "*");
  }

  public LinkedList<PitchNode> getMelody() {

    return new LinkedList(this.pitch_route.stream()
            .map(ph -> ph.getSelected().getTo()).collect(Collectors.toList()));
  }

  /*
     * Default getters and setters.
   */
  public PitchNode getFinalis() {

    return finalis;
  }

  public void setFinalis(PitchNode finalis) {

    this.finalis = finalis;
  }

  public PitchNode getDominant() {

    return dominant;
  }

  public void setDominant(PitchNode dominant) {

    this.dominant = dominant;
  }

  public EcclesiasticalMode getEcclesiastical_Mode() {

    return ecclesiastical_mode;
  }

  public void setMode(EcclesiasticalMode ecclesiastical_mode) {

    this.ecclesiastical_mode = ecclesiastical_mode;
  }

  public LinkedList<PitchMove> getHistory() {

    return pitch_route;
  }

  public void setHistory(LinkedList<PitchMove> history) {

    this.pitch_route = history;
  }

}
