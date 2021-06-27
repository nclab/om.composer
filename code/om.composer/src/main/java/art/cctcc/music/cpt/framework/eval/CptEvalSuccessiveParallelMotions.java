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

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.CptCounterpoint;
import art.cctcc.music.cpt.model.enums.CptContrapuntalMotion;
import static art.cctcc.music.cpt.model.enums.CptContrapuntalMotion.Parallel;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalSuccessiveParallelMotions extends CptEval<CptCounterpoint> {

  public CptEvalSuccessiveParallelMotions(CptCounterpoint cpt) {

    super(cpt);
  }

  @Override
  public double eval() {

    var motions = IntStream.range(0, t.length() - 1)
            .mapToObj(
                    i -> new CptPitchPath[]{
                      CptPitchPath.of(t.getNote(i), t.getNote(i + 1)),
                      CptPitchPath.of(t.getCf().getNote(i), t.getCf().getNote(i + 1))}
            )
            .map(CptContrapuntalMotion::getContrapuntalMotion)
            .toArray(CptContrapuntalMotion[]::new);
    var dim = new int[motions.length + 1];
    IntStream.rangeClosed(1, motions.length)
            .filter(i -> Parallel.equals(motions[i - 1]))
            .forEach(i -> dim[i] = dim[i - 1] + 1);
    return switch (Arrays.stream(dim).max().getAsInt()) {
      case 0, 1, 2: yield 1.0;
      case 3: yield 0.5;
      case 4: yield 0.25;
      default: yield 0.0;
    };
  }
}
