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
package art.cctcc.music.cpt.graphs.y_cpt;

import art.cctcc.music.cpt.model.enums.CptPitch;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.*;
import java.util.List;
import tech.metacontext.ocnhfa.antsomg.impl.StandardEdge;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptPitchPath extends StandardEdge<CptPitchNode> {

  public static CptPitchPath of(CptPitchNode from, CptPitchNode to) {

    return new CptPitchPath(from, to, 1.0);
  }

  public CptPitchPath(CptPitchNode from, CptPitchNode to, double cost) {

    super(from, to, cost);
  }

  public CptPitchPath(CptPitchPath path) {

    super(path.getFrom(), path.getTo(), path.getCost());
  }

  public int absDiff() {

    return Math.abs(CptPitch.diff(this));
  }

  public boolean melodicFeasible() {

    var diff = CptPitch.diatonicDiff(this) % 7;
    return switch (CptPitch.quality(this)) {
      case Perfect: yield true;
      case Major: yield List.of(1, 2, -1, -2).contains(diff);
      case Minor: yield List.of(1, 2, 5, -1, -2).contains(diff);
      default: yield false;
    };
  }

  @Override
  public int hashCode() {

    var from = this.getFrom() == null ? 0 : this.getFrom().getPitch().name().hashCode();
    var to = this.getTo().getPitch().name().hashCode() * 57;
    return from + to;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (obj instanceof CptPitchPath cpp) {
      return this.getFrom().equals(cpp.getFrom()) && this.getTo().equals(cpp.getTo());
    }
    return false;
  }
}
