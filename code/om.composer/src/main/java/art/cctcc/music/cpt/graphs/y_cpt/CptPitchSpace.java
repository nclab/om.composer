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

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.Objects;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedIntervalException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptPitchSpace extends StandardGraph<CptPitchPath, CptPitchNode> {

  private static CptPitchSpace cptps;

  public static CptPitchSpace getInstance() {

    if (Objects.isNull(cptps)) {
      cptps = new CptPitchSpace();
      cptps.init_graph();
    }
    return cptps;
  }

  CptPitchSpace() {

    super(ALPHA, BETA);
  }

  @Override
  public void init_graph() {

    CptPitch.diatonicValues().stream()
            .forEach(pitch -> {
              IntStream.of(1, 2, 3, 4, 5, 6, 8, -2, -3, -4, -5, -8)
                      .mapToObj(i -> getPitchPath(pitch, i))
                      .filter(Objects::nonNull)
                      .forEach(this::addEdges);
            });
  }

  public CptPitchPath getPitchPath(CptPitch pitch, int interval) {

    if (interval == 6 && pitch.name().matches("^[CDFG][0-9]$")) {
      return null;
    }
    try {
      var target_index = CptPitch.diatonicValues().indexOf(pitch)
              + (interval > 0 ? interval - 1 : interval + 1);
      var target = CptPitch.diatonicValues().get(target_index);
      var cost = getCostByInterval(interval);
      return pitch.tritone(target) ? null
              : new CptPitchPath(pitch.getNode(), target.getNode(), cost);
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  public static double getCostByInterval(int interval)
          throws UnexpectedIntervalException {

    return switch (Math.abs(interval)) {
      case 2, 3: yield 1.0;
      case 1, 5, 8: yield 4.0;
      case 4: yield 8.0;
      case 6: yield 16.0;
      default: throw new UnexpectedIntervalException(interval);
    };
  }
}
