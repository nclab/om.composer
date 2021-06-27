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
package art.cctcc.music.utils.ec;

import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.utils.CptCalculator;
import java.util.Map;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Evaluator;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.constraints.MusicThreadRating;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CF_Evaluator implements Evaluator<MusicThread> {

  @Override
  public Double apply(MusicThread t) {

    var rating = rating(t);
    var validity = validity(t);
    return Math.pow(rating, validity);
  }

  public Double rating(MusicThread t) {

    var cf = new CptCantusFirmus(null, t.getCf());
    return CptCalculator.cfRating(Map.entry(cf, CptCalculator.countBothSides(cf)))
            .doubleValue();
  }

  public Double validity(MusicThread t) {

    return MusicThreadRating.rate(t) / 100.0;
  }
}
