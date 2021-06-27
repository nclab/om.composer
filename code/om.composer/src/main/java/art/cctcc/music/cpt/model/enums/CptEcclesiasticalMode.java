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
package art.cctcc.music.cpt.model.enums;

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.model.CptCadence;
import static art.cctcc.music.cpt.model.enums.CptPitch.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum CptEcclesiasticalMode {

  Dorian(List.of(Cs3, D3), List.of(Cs4, D4), List.of(Cs5, D5)),
  Phrygian(List.of(D3, E3), List.of(D4, E4), List.of(D5, E5)),
  Mixolydian(List.of(Fs3, G3), List.of(Fs4, G4), List.of(Fs5, G5)),
  Aeolian(List.of(Gs2, A2), List.of(Gs3, A3), List.of(Gs4, A4)),
  Ionian(List.of(B2, C3), List.of(B3, C4), List.of(B4, C5));

  private final List<CptCadence> cadences;

  private CptEcclesiasticalMode(List<CptPitch>... cadences) {

    this.cadences = Stream.of(cadences)
            .map(c -> new CptCadence(c, this))
            .collect(Collectors.toList());
  }

  public List<CptPitchNode> getTerminals(boolean includingDominants) {

    var filter = getTerminalFilter(this, includingDominants);
    return CptPitch.diatonicValues().stream()
            .filter(p -> p.name().matches(filter))
            .map(CptPitch::getNode)
            .collect(Collectors.toList());
  }

  public static String getTerminalFilter(CptEcclesiasticalMode mode, boolean includingDominants) {

    return switch (mode) {
      case Dorian: yield includingDominants ? "[DA]." : "[D].";
      case Phrygian: yield includingDominants ? "[EB]." : "[E].";
      case Mixolydian: yield includingDominants ? "[GD]." : "[G].";
      case Aeolian: yield includingDominants ? "[AE]." : "[A].";
      case Ionian: yield includingDominants ? "[CG]." : "[C].";
    };
  }

  public List<CptCadence> getCadences() {

    return cadences;
  }
}
