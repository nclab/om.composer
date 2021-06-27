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
package art.cctcc.music.cpt.graphs.y_cpt;

import art.cctcc.music.cpt.model.enums.CptPitch;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Major;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Minor;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.Perfect;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedIntervalException;

/**
 * Add-on for ICCC.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptPitchSpaceChromatic extends CptPitchSpace {

  public static boolean allowEnharmonic;

  private static CptPitchSpaceChromatic cptps;

  public static CptPitchSpaceChromatic getInstance() {

    if (Objects.isNull(cptps)) {
      cptps = new CptPitchSpaceChromatic();
      cptps.init_graph();
    }
    return cptps;
  }

  @Override
  public void init_graph() {

    Stream.of(CptPitch.values())
            .forEach(pitch0 -> {
              // 0 is for compatibility with RECITING mode
              IntStream.of(0, 1, 2, 3, 4, 5, 7, 8, 12, -1, -2, -3, -4, -5, -7, -12)
                      .mapToObj(steps -> getPitchPath(pitch0, steps))
                      .filter(Objects::nonNull)
                      .forEach(this::addEdges);
            });
  }

  @Override
  public CptPitchPath getPitchPath(CptPitch pitch0, int steps) {

    try {
      var pitch1 = Stream.of(CptPitch.values())
              .map(CptPitch::getNode)
              .filter(pitch -> CptPitch.diff(pitch0.getNode(), pitch) == steps)
              .filter(pitch -> List.of(Major, Minor, Perfect).contains(CptPitch.quality(pitch0.getNode(), pitch)))
              .findFirst().get();
      var cost = getCostBySteps(steps);
      return new CptPitchPath(pitch0.getNode(), pitch1, cost);
    } catch (IndexOutOfBoundsException | NoSuchElementException ex) {
      return null;
    }
  }

  public static double getCostBySteps(int interval)
          throws UnexpectedIntervalException {

    return switch (Math.abs(interval)) {
      case 1, 2, 3, 4: yield 1.0; // minor 2nd, major 2nd, minor 3rd, major 3rd
      case 0, 7, 12: yield 4.0; // perfect 1st, 5th, 8th
      case 5: yield 8.0; // perfect 4th
      case 8: yield 16.0;// minor 6th
      default: throw new UnexpectedIntervalException(interval);
    };
  }
}
