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

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode;
import art.cctcc.music.cpt.model.enums.CptPitch;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Augmented;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptCadence {

  private final LinkedList<CptPitchNode> formula;
  private final CptEcclesiasticalMode mode;

  public CptCadence(List<CptPitch> formula, CptEcclesiasticalMode mode) {

    this.formula = new LinkedList<>(
            formula.stream()
                    .map(CptPitch::getNode)
                    .collect(Collectors.toList())
    );
    this.mode = mode;
  }

  public CptPitchPath getPitchPath() {

    return new CptPitchPath(formula.getFirst(), formula.getLast(), 1.0);
  }

  /**
   * Get Path if the given pitch is suitable to connect to this Cadence.
   *
   * @param pitch
   * @return the <code>CptPitchPath</code> to the first pitch of this Cadence if
   * suitable; <code>null</code> if not.
   */
  public CptPitchPath getPathToCadence(CptPitchNode pitch) {

    var first = this.formula.getFirst();
    var diff = CptPitch.diff(pitch, first);
    return switch (this.mode) {
      case Dorian, Mixolydian, Ionian ->
        switch (diff) {
          case -3, -1, 2: yield CptPitchPath.of(pitch.getPitch().getNode(), first);
          default: yield null;
        };
      case Phrygian ->
        switch (diff) {
          case -3, -2, 2: yield CptPitchPath.of(pitch.getPitch().getNode(), first);
          default: yield null;
        };
      case Aeolian ->
        switch (diff) {
          case -3, -1: yield CptPitchPath.of(pitch.getPitch().getNode(), first);
          case 3:
            yield CptPitchPath.of(CptPitch.values()[pitch.getPitch().ordinal() + 1].getNode(), first);
          default: yield null;
        };
    };
  }

  @Override
  public String toString() {

    return "Cadence{" + "formula=" + formula + '}';
  }

  /*
     * Default getters and setters.
   */
  public LinkedList<CptPitchNode> getFormula() {

    return formula;
  }
}
