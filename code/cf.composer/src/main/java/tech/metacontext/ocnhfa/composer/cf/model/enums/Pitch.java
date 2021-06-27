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

import java.util.Objects;
import org.audiveris.proxymusic.Step;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedIntervalException;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchPath;

/**
 * Pitch gamut according to the cantus firmi in Knud Jeppesen: "Counterpoint,
 * the Polyphonic Vocal Style of the Sixteenth Century".
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Pitch {

  D3, E3, F3, G3, A3, B3, C4, D4, E4, F4, G4, A4;

  private final PitchNode node;

  public Step getStep() {

    return Step.fromValue(this.name().substring(0, 1));
  }

  public int getOctave() {

    var len = this.name().length();
    return Integer.valueOf(this.name().substring(len - 1, len));
  }

  private Pitch() {

    this.node = new PitchNode(this);
  }

  public PitchNode getNode() {

    return node;
  }

  public PitchPath up(int interval) {

    return (interval == 6 && this.name().matches("[CDFG]."))
            ? null : getPitchPath(this.ordinal() + interval - 1);
  }

  public PitchPath down(int interval) {

    return getPitchPath(this.ordinal() - interval + 1);
  }

  public PitchPath getPitchPath(int target_ordinal) {

    try {
      var target = Pitch.values()[target_ordinal];
      var cost = getCostByInterval(Math.abs(this.ordinal() - target_ordinal) + 1);
      return tritoneTest(target) ? null
              : new PitchPath(this.node, target.node, cost);
    } catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }

  public boolean tritoneTest(Pitch target) {

    var join = this.name() + target.name();
    return (join.matches("B.F.") || join.matches("F.B."));
  }

  public static double getCostByInterval(int interval)
          throws UnexpectedIntervalException {

    return switch (interval) {
      case 2, 3 ->
        1.0;
      case 5, 8 ->
        4.0;
      case 4 ->
        8.0;
      case 6 ->
        16.0;
      default ->
        throw new UnexpectedIntervalException(interval);
    };
  }

  public static int diff(PitchNode from, PitchNode to) {

    var from_ordinal = from.getPitch().ordinal();
    var to_ordinal = to.getPitch().ordinal();
    var diff_raw = to_ordinal - from_ordinal;
    return diff_raw + ((diff_raw >= 0) ? 1 : -1);
  }

  public static int diff(PitchPath path) {

    return Objects.isNull(path)
            ? 0 : diff(path.getFrom(), path.getTo());
  }

  public static Pitch valueOf(MusicNode node) {

    return Pitch.valueOf(node.getName());
  }

  public EcclesiasticalMode asFinalis() {

    return switch (this) {
      case A3,A4 ->
        EcclesiasticalMode.Aeolian;
      case C4 ->
        EcclesiasticalMode.Ionian;
      case D3,D4 ->
        EcclesiasticalMode.Dorian;
      case E3,E4 ->
        EcclesiasticalMode.Phrygian;
      case G3,G4 ->
        EcclesiasticalMode.Mixolydian;
      default ->
        null;
    };
  }
}
