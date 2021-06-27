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
package art.cctcc.music.cpt.framework.eval;

import art.cctcc.music.cpt.model.CptMelody;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.HashMap;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalProperRepetition extends CptEval<CptMelody> {

  public CptEvalProperRepetition(CptMelody t) {

    super(t);
  }

  @Override
  public double eval() {

    var score = 1.0;
    var map = new HashMap<CptPitch, Integer>();
    for (int i = 0; i < t.getMelody().size(); i++) {
      var node = t.getNote(i).getPitch().getNatural();
      if (i == 0 || !node.equals(t.getNote(i - 1).getPitch().getNatural())) {
        var count = map.getOrDefault(node, 0);
        map.put(node, count + 3);
        if (map.values().stream().anyMatch(v -> v > 4)) {
          return 0.0;
        }
        if (map.values().stream().filter(v -> v >= 3).count() > 1) {
          score /= 2.0;
        }
      }
      map.replaceAll((n, v) -> v > 0 ? v - 1 : 0);
    }
    return score;
  }
}
