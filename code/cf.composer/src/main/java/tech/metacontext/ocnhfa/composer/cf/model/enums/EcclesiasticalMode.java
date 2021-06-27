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
package tech.metacontext.ocnhfa.composer.cf.model.enums;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardParameters;
import tech.metacontext.ocnhfa.composer.cf.model.devices.Cadence;
import static tech.metacontext.ocnhfa.composer.cf.model.enums.EcclesiasticalMode.values;
import static tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch.*;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum EcclesiasticalMode {

  Dorian(List.of(D3, D4), List.of(A3, A4)),
  Phrygian(List.of(E3, E4), List.of(C4, A4)),
  Mixolydian(List.of(G3, G4), List.of(D3, D4)),
  Aeolian(List.of(A3, A4), List.of(E3, E4)),
  Ionian(List.of(C4), List.of(G4)),
  RANDOM_MODE;

  private Map<PitchNode, PitchNode> dominants;
  private List<Cadence> cadences;

  EcclesiasticalMode() {

  }

  EcclesiasticalMode(List<Pitch> finalis, List<Pitch> dominants) {

    this.dominants = IntStream.range(0, finalis.size())
            .mapToObj(i -> Map.entry(finalis.get(i).getNode(), dominants.get(i).getNode()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    this.cadences = finalis.stream()
            .filter(pn -> pn.ordinal() + 1 < Pitch.values().length)
            .map(PitchNode::new)
            .map(Cadence::new)
            .collect(Collectors.toList());
  }

  public static EcclesiasticalMode getRandomMode() {

    return values()[StandardParameters.getRandom().nextInt(EcclesiasticalMode.values().length - 1)];
  }

  public Entry<PitchNode, PitchNode> getRandomFinalis() {

    var index = StandardParameters.getRandom().nextInt(dominants.size());
    var result = dominants.keySet().toArray(new PitchNode[0])[index];
    return Map.entry(result, dominants.get(result));
  }

  public Map<PitchNode, PitchNode> getDominants() {

    return this.dominants;
  }

  public Entry<PitchNode, PitchNode> getDominant(PitchNode finalis) {

    return this.dominants.entrySet().stream()
            .filter(e -> e.getKey().equals(finalis))
            .findFirst().orElse(null);
  }

  public List<Cadence> getCadences() {

    return cadences;
  }

  public Cadence getCadence(PitchNode pitch) {

    return this.getCadences().stream()
            .filter(ca -> {
              var diff = Pitch.diff(pitch, ca.getFormula().get(0));
              return switch (this) {
                case Dorian ->
                  diff == -2 || diff == 2;
                case Phrygian ->
                  diff == -2 || diff == 2 || diff == 3;
                case Mixolydian ->
                  diff == -2 || diff == 2 || diff == 4;
                case Aeolian ->
                  diff == -2 || diff == 2;
                case Ionian ->
                  diff == -2 || diff == 2;
                default ->
                  false;
              };
            }).findAny().orElse(null);
  }
}
