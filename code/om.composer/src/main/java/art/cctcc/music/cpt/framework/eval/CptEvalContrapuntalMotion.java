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

import art.cctcc.music.cpt.model.CptCounterpoint;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalContrapuntalMotion extends CptEval<CptCounterpoint> {

  public CptEvalContrapuntalMotion(CptCounterpoint cpt) {

    super(cpt);
  }

  @Override
  public double eval() {

    var ideal_contrary = t.length() / 2.0;
    var actual_contrary = 1.0 * IntStream.range(0, t.length() - 2)
            .filter(locus -> t.getDiffAtLocus(locus) * t.getCf().getDiffAtLocus(locus) < 0)
            .count();
    var score = actual_contrary / ideal_contrary;
    return actual_contrary > ideal_contrary ? 2.0 - score : score;
  }
}
