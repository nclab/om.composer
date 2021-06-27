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

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvalRange extends CptEval<CptMelody> {

  public CptEvalRange(CptMelody melody) {

    super(melody);
  }

  @Override
  public double eval() {

    return switch (t.pitchRange()) {
      case 7, 8, 9, 12: yield 1.0;
      case 5, 14: yield 0.5;
      case 15, 16: yield 0.25;
      default: yield 0.0;
    };
  }
}
