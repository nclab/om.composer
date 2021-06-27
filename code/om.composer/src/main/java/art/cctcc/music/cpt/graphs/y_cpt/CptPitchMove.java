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

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptPitchMove extends StandardMove<CptPitchPath> {

  /**
   * Constructor of CptPitchMove.
   *
   * @param exploring
   * @param edges
   * @param selected
   */
  public CptPitchMove(boolean exploring, List<CptPitchPath> edges, CptPitchPath selected) {

    super(exploring, edges, selected);
  }

  public CptPitchMove(StandardMove<CptPitchPath> move) {

    super(move.isExploring(), move.getSelected());
    this.getPheromoneRecords().putAll(move.getPheromoneRecords());
  }

  /**
   * Constructor of CptPitchMove, only for entry.
   *
   * @param entry
   */
  public CptPitchMove(CptPitchNode entry) {

    super(new CptPitchPath(null, entry, 0.0));
  }

  @Override
  public String toString() {

    var ph = this.getPheromoneRecords().entrySet().stream()
            .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
            .map(entry -> String.format("->%s(%.3f)", entry.getKey().getTo(), entry.getValue()))
            .collect(Collectors.joining("; "));
    return String.format("-> %s { Explore=%5b, Pheromone=[%s] }",
            this.getSelected().getTo(), this.isExploring(), ph);
  }
}
