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
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import static art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode.*;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.*;
import java.util.List;
import java.util.Objects;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum CptPitch {

  F2(0), Fs2(1), G2(2), Gs2(3), A2(4), As2(5), Bf2(5), B2(6),
  C3(7), Cs3(8), D3(9), Ds3(10), E3(11), F3(12),
  Fs3(13), G3(14), Gs3(15), A3(16), As3(17), Bf3(17), B3(18),
  C4(19), Cs4(20), D4(21), Ds4(22), E4(23), F4(24),
  Fs4(25), G4(26), Gs4(27), A4(28), As4(29), Bf4(29), B4(30),
  C5(31), Cs5(32), D5(33), Ds5(34), E5(35), F5(36),
  Fs5(37), G5(38);

  private final int chromatic_number;
  private final CptPitchNode node;

  private CptPitch(int chromatic_number) {

    this.chromatic_number = chromatic_number;
    this.node = new CptPitchNode(this);
  }

  public CptPitchNode getNode() {

    return node;
  }

  public int getChromatic_number() {

    return chromatic_number;
  }

  public static List<CptPitch> diatonicValues() {

    return Stream.of(values())
            .filter(not(p -> p.name().matches(".[sf].")))
            .collect(Collectors.toList());
  }

  public boolean tritone(CptPitch target) {

    var join = this.name() + target.name();
    return (join.contains("B") && join.contains("F"));
  }

  public static int diff(CptPitchNode from, CptPitchNode to) {

    var to_chr = to.getPitch().getChromatic_number();
    var from_chr = from.getPitch().getChromatic_number();
    var diff_raw = to_chr - from_chr;
    return diff_raw;
  }

  public static Integer diff(CptPitchPath path) {

    return Objects.isNull(path)
            ? null : diff(path.getFrom(), path.getTo());
  }

  public CptPitch getNatural() {

    return CptPitch.valueOf(this.name().replaceAll("[sf]", ""));
  }

  public static Integer diatonicDiff(CptPitchPath path) {

    return Objects.isNull(path) ? null : diatonicDiff(path.getFrom(), path.getTo());
  }

  public static int diatonicDiff(CptPitchNode from, CptPitchNode to) {

    var from_dia = diatonicValues().indexOf(from.getPitch().getNatural());
    var to_dia = diatonicValues().indexOf(to.getPitch().getNatural());
    return to_dia - from_dia;
  }

  public String getStep() {

    return this.name().substring(0, 1);
  }

  public int getOctave() {

    var len = this.name().length();
    return Integer.valueOf(this.name().substring(len - 1, len));
  }

  public String getAccidental() {

    return switch (this.name().length()) {
      case 3: yield switch (this.name().substring(1, 2)) {
        case "s": yield "sharp";
        case "f": yield "flat";
        default: yield "";
      };
      default: yield "";
    };
  }

  public CptEcclesiasticalMode getMode() {

    var name = this.getAccidental().isEmpty() ? this.getStep() : null;
    return switch (name) {
      case "C": yield Ionian;
      case "D": yield Dorian;
      case "E": yield Phrygian;
      case "G": yield Mixolydian;
      case "A": yield Aeolian;
      default: yield null;
    };
  }

  public static IntervalQuality quality(CptPitchPath path) {

    return quality(path.getFrom(), path.getTo());
  }

  public static IntervalQuality quality(CptPitchNode from, CptPitchNode to) {

    var degrees = Math.abs(diatonicDiff(from, to)) % 7;
    var steps = Math.abs(diff(from, to)) % 12;
    return switch (degrees) {
      case 0: {
        yield switch (steps) {
          case 11: yield Diminished;
          case 0: yield Perfect;
          case 1: yield Augmented;
          default: yield Other;
        };
      }
      case 1: {
        yield switch (steps) {
          case 0: yield Diminished;
          case 1: yield Minor;
          case 2: yield Major;
          case 3: yield Augmented;
          default: yield Other;
        };
      }
      case 2: {
        yield switch (steps) {
          case 2: yield Diminished;
          case 3: yield Minor;
          case 4: yield Major;
          case 5: yield Augmented;
          default: yield Other;
        };
      }
      case 3: {
        yield switch (steps) {
          case 4: yield Diminished;
          case 5: yield Perfect;
          case 6: yield Augmented;
          default: yield Other;
        };
      }
      case 4: {
        yield switch (steps) {
          case 6: yield Diminished;
          case 7: yield Perfect;
          case 8: yield Augmented;
          default: yield Other;
        };
      }
      case 5: {
        yield switch (steps) {
          case 7: yield Diminished;
          case 8: yield Minor;
          case 9: yield Major;
          case 10: yield Augmented;
          default: yield Other;
        };
      }
      case 6: {
        yield switch (steps) {
          case 9: yield Diminished;
          case 10: yield Minor;
          case 11: yield Major;
          case 0: yield Augmented;
          default: yield Other;
        };
      }
      default: {
        yield Other;
      }
    };
  }
}
