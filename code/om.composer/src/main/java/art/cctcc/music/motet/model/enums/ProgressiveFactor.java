/*
 * Copyright 2020 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music.motet.model.enums;

import art.cctcc.music.cpt.model.CptCantusFirmus;
import java.util.function.Function;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum ProgressiveFactor {

  CF_LENGTH(CptCantusFirmus::length),
  CF_RANGE(CptCantusFirmus::pitchRange);

  public final Function<CptCantusFirmus, Integer> score;

  private ProgressiveFactor(Function<CptCantusFirmus, Integer> score) {

    this.score = score;
  }

  public double eval(CptCantusFirmus cf) {

    return this.score.apply(cf);
  }

  public static final Function<CptCantusFirmus, Double> CF_SCORE = cf -> CF_LENGTH.eval(cf) * 10 + CF_RANGE.eval(cf);
}
