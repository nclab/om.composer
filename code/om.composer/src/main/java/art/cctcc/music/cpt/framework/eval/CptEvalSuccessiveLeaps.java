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
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalSuccessiveLeaps extends CptEval<CptMelody> {

  public CptEvalSuccessiveLeaps(CptMelody t) {

    super(t);
  }

  @Override
  public double eval() {

    int disjunct_counter = 0;
    int avoided_leap_counter = 0;
    var melody = t.getMelody();
    var melody_array = IntStream.range(1, melody.size())
            .map(i -> CptPitch.diatonicDiff(melody.get(i - 1), melody.get(i)))
            .toArray();
    for (int i = 0; i < melody_array.length; i++) {
      var current = melody_array[i];
      if (Math.abs(current) > 2) {
        disjunct_counter++;
      }
      if (current > 1 && i > 0) {
        var previous = melody_array[i - 1];
        if (previous > 0
                && (current == 2 && current > previous || current > 2 && current >= previous)) {
          avoided_leap_counter++;
        }
      } else if (current < -1 && i < melody_array.length - 1) {
        var next = melody_array[i + 1];
        if (next < 0
                && (current == -2 && current < next || current < -2 && current <= next)) {
          avoided_leap_counter++;
        }
      }
    }
    return switch (avoided_leap_counter + disjunct_counter) {
      case 1, 2: yield 1.0;
      case 0, 3: yield 0.5;
      case 4, 5: yield 0.25;
      default: yield 0.0;
    };
  }
}
