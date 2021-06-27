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

import art.cctcc.music.cpt.framework.CptThread;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.enums.CptContrapuntalMotion;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalRetainingNotes extends CptEval<CptThread> {

  public CptEvalRetainingNotes(CptThread thread) {

    super(thread);
  }

  @Override
  public double eval() {

    var same_pitch_count = (int) IntStream.range(1, t.getCpt().length())
            .mapToObj(i -> new CptPitchPath(t.getCpt().getNote(i - 1), t.getCpt().getNote(i), 0.0))
            .filter(CptContrapuntalMotion::samePitch)
            .count();
    return switch (same_pitch_count) {
      case 0, 1: yield 1.0;
      case 2: yield 0.5;
      case 3: yield 0.25;
      default: yield 0.0;
    };
  }
}
