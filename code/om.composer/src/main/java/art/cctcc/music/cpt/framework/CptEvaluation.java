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
package art.cctcc.music.cpt.framework;

import art.cctcc.music.cpt.framework.eval.CptEval;
import art.cctcc.music.cpt.framework.eval.CptEvalChromaticism;
import art.cctcc.music.cpt.framework.eval.CptEvalContrapuntalMotion;
import art.cctcc.music.cpt.framework.eval.CptEvalProperRepetition;
import art.cctcc.music.cpt.framework.eval.CptEvalRange;
import art.cctcc.music.cpt.framework.eval.CptEvalRetainingNotes;
import art.cctcc.music.cpt.framework.eval.CptEvalSuccessiveLeaps;
import art.cctcc.music.cpt.framework.eval.CptEvalSuccessiveParallelMotions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvaluation {

  public enum EvalType {

    traditional, chromatic
  }

  public static EvalType type = EvalType.traditional;

  private final Map<Supplier<CptEval>, Double> eval_fns;
  private final double base;

  public static CptEvaluation getInstance(CptThread thread) {

    var instance = new CptEvaluation(thread);
    return instance;
  }

  private CptEvaluation(CptThread thread) {

    this.eval_fns = new HashMap<>(Map.of(
            () -> new CptEvalContrapuntalMotion(thread.getCpt()), 3.0,
            () -> new CptEvalProperRepetition(thread.getCpt()), 3.0,
            () -> new CptEvalSuccessiveParallelMotions(thread.getCpt()), 2.0,
            () -> new CptEvalSuccessiveLeaps(thread.getCpt()), 1.0,
            () -> new CptEvalRange(thread.getCpt()), 1.0));

    switch (type) {
      case traditional ->
        this.eval_fns.put(() -> new CptEvalRetainingNotes(thread), 1.0);
      case chromatic ->
        this.eval_fns.put(() -> new CptEvalChromaticism(thread.getCpt()), 3.0);
    }

    this.base = this.eval_fns.values().stream()
            .reduce(Double::sum).get();
  }

  public double get() {

    var raw = eval_fns.entrySet().stream()
            .mapToDouble(CptEvaluation::eval).sum();
    return BigDecimal.valueOf(raw / this.base)
            .setScale(8, RoundingMode.HALF_UP)
            .doubleValue();
  }

  public static double eval(Entry<Supplier<CptEval>, Double> e) {

    return e.getKey().get().eval() * e.getValue();
  }
}
