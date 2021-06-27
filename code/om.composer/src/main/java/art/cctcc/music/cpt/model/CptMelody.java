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
package art.cctcc.music.cpt.model;

import art.cctcc.music.cpt.ex.InvalidPitchType;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptMelody {

  private String id;
  private LinkedList<CptPitchNode> melody;

  private CptEcclesiasticalMode mode;

  public CptMelody(String id) {

    this.id = id;
    this.melody = new LinkedList<>();
  }

  /**
   * Constructor of CptMelody with melody.
   *
   * @param id
   * @param melody List of CptPitchNode or CptPitch
   */
  public CptMelody(String id, List melody) {

    this(id);
    var p0 = melody.get(0);
    if (p0 instanceof CptPitchNode p) {
      this.melody.addAll(melody);
      if (p.getPitch() != null) {
        this.setMode(p.getPitch().getMode());
      }
    } else if (p0 instanceof CptPitch p) {
      this.addAllPitches(melody);
      if (p != null) {
        this.setMode(p.getMode());
      }
    } else if (p0 instanceof Pitch p) {
      var melody0 = ((List<Pitch>) melody).stream()
              .map(o -> (Pitch) o)
              .map(Pitch::name)
              .map(CptPitch::valueOf)
              .collect(Collectors.toList());
      this.addAllPitches(melody0);
      this.setMode(melody0.get(0).getMode());
    } else {
      throw new InvalidPitchType(this.melody.get(0).getClass().getSimpleName());
    }
  }

  public static CptMelody getTacetMelody(int length) {

    return new CptMelody("tacet",
            Stream.generate(CptPitchNode::getEmptyNode)
                    .limit(length)
                    .collect(Collectors.toList()));
  }

  public CptPitch getMiddle() {

    var summary = this.melody.stream()
            .map(CptPitchNode::getPitch)
            .map(CptPitch::getNatural)
            .mapToInt(CptPitch.diatonicValues()::indexOf)
            .summaryStatistics();
    var min = summary.getMin();
    var max = summary.getMax();
    return CptPitch.diatonicValues().get(min + (max - min) / 2);
  }

  public int getDiffAtLocus(int locus) {

    var from = melody.get(locus);
    var to = melody.get(locus + 1);
    return CptPitch.diff(from, to);
  }

  public int getDiatonicDiffAtLocus(int locus) {

    var from = melody.get(locus);
    var to = melody.get(locus + 1);
    return CptPitch.diatonicDiff(from, to);
  }

  public int pitchRange() {

    var summary = this.melody.stream()
            .map(CptPitchNode::getPitch)
            .mapToInt(CptPitch::getChromatic_number)
            .summaryStatistics();
    return summary.getMax() - summary.getMin();
  }

  public int length() {

    return this.melody.size();
  }

  @Override
  public String toString() {

    return this.getClass().getSimpleName() + "{" + "melody=" + melody + '}';
  }

  public void addNote(CptPitchNode p) {

    this.melody.add(p);
  }

  public CptPitchNode getNote(int locus) {

    try {
      return this.melody.get(locus);
    } catch (Exception e) {
      return null;
    }
  }

  public final void addAllPitches(List<CptPitch> pitches) {

    pitches.stream().map(CptPitch::getNode)
            .forEach(this::addNote);
  }

  public LinkedList<CptPitchNode> getMelody() {

    return melody;
  }

  public void setMelody(LinkedList<CptPitchNode> melody) {

    this.melody = melody;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public CptEcclesiasticalMode getMode() {

    return mode;
  }

  public void setMode(CptEcclesiasticalMode mode) {

    this.mode = mode;
  }
}
