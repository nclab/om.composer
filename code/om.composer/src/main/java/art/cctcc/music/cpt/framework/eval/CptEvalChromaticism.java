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
import java.util.Arrays;

/**
 * Add-on for ICCC.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalChromaticism extends CptEval<CptCounterpoint> {

  public CptEvalChromaticism(CptCounterpoint cpt) {

    super(cpt);
  }

  @Override
  public double eval() {

    var chromatics = new Boolean[12];
    Arrays.fill(chromatics, false);
    t.getMelody().stream()
            .map(p -> p.getPitch().getChromatic_number() % 12)
            .forEach(c -> chromatics[c] = true);
    var count = Arrays.stream(chromatics).filter(c -> c).count() - 5;
    return count < 0 ? 0 : count / 7.0;
  }
}
