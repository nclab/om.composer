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

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Augmented;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Diminished;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum CptContrapuntalMotion {

  Ascending, Descending, Same, Parallel, Contrary, Oblique, Similar;

  /**
   *
   * @param paths
   * @return
   */
  public static CptContrapuntalMotion getContrapuntalMotion(CptPitchPath... paths) {

    if (Objects.isNull(paths[0]) || Objects.isNull(paths[1])) {
      return null;
    }
    var diff0 = CptPitch.diff(paths[0]);
    var diff1 = CptPitch.diff(paths[1]);
    if (diff0 == 0 && diff1 == 0) {
      return Same;
    }
    var motion = diff0 * diff1;
    if (motion == 0) {
      return Oblique;
    }
    if (motion < 0) {
      return Contrary;
    }
    var dia_diff0 = CptPitch.diatonicDiff(paths[0]);
    var dia_diff1 = CptPitch.diatonicDiff(paths[1]);
    return Objects.equals(dia_diff0, dia_diff1) ? Parallel : Similar;
  }

  /**
   * Show if provided pitch paths are in forbidden parallel motion.
   *
   * @param paths paths to be compared, only 2 paths will be taken.
   * @return true if they are forbidden.
   */
  public static boolean isForbiddenParallel(CptPitchPath... paths) {

    var from = Math.abs(CptPitch.diff(paths[0].getFrom(), paths[1].getFrom())) % 12;
    var to = Math.abs(CptPitch.diff(paths[0].getTo(), paths[1].getTo())) % 12;
    return (from == 0 && to == 0) || (from == 7 && to == 7);
  }

  /**
   * Show if provided pitch paths are in forbidden hidden parallel motion.
   *
   * @param paths paths to be compared, only 2 paths will be taken.
   * @return true if they are forbidden.
   */
  public static boolean isForbiddenHiddenParallel(CptPitchPath... paths) {

    var to = Math.abs(CptPitch.diff(paths[0].getTo(), paths[1].getTo())) % 12;
    return (to == 0 || to == 7) && isSimilar(paths);
  }

  public static boolean isForbiddenLeap(CptPitchPath... paths) {

    return isSimilar(paths[0], paths[1])
            && (leap(paths[0]) && leapOverFourth(paths[1])
            || leap(paths[1]) && leapOverFourth(paths[0]));
  }

  public static boolean leap(CptPitchPath path) {

    return Math.abs(CptPitch.diatonicDiff(path)) >= 2;
  }

  public static boolean leapOverFourth(CptPitchPath path) {

    return path.absDiff() > 5 && path.absDiff() % 12 > 0;
  }

  public static boolean hasDevilAcrossVoices(CptPitchPath... paths) {

    var cross1 = CptPitchPath.of(paths[0].getFrom(), paths[1].getTo());
    var cross2 = CptPitchPath.of(paths[1].getFrom(), paths[0].getTo());
    return exposeDevil(cross1) || exposeDevil(cross2);
  }

  public static boolean exposeDevil(CptPitchPath cross) {

    var quality = CptPitch.quality(cross);
    return switch (CptPitch.diatonicDiff(cross) % 7) {
      case 0: yield List.of(Augmented, Diminished).contains(quality);
      case -3, 3: yield Augmented.equals(quality);
      case -4, 4: yield Diminished.equals(quality);
      default: yield false;
    };
  }

  public static boolean isSimilar(CptPitchPath... paths) {

    return !samePitch(paths[0])
            && Stream.of(paths)
                    .map(CptContrapuntalMotion::melodicMotionType)
                    .allMatch(melodicMotionType(paths[0])::equals);
  }

  /**
   * Show if provided pitch paths are in contrary motion.
   *
   * @param paths paths to be compared, only 2 paths will be taken.
   * @return true if they are in contrary motion.
   */
  public static boolean isContrary(CptPitchPath... paths) {

    return CptPitch.diff(paths[0]) * CptPitch.diff(paths[1]) < 0;
  }

  public static CptContrapuntalMotion melodicMotionType(CptPitchPath path) {

    if (CptPitch.diff(path) > 0) {
      return Ascending;
    }
    if (CptPitch.diff(path) < 0) {
      return Descending;
    }
    return Same;
  }

  public static boolean samePitch(CptPitchPath path) {

    return melodicMotionType(path) == Same;
  }
}
