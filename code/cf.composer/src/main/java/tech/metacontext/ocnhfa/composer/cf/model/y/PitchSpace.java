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
package tech.metacontext.ocnhfa.composer.cf.model.y;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.Y_ALPHA;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.Y_BETA;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PitchSpace extends StandardGraph<PitchPath, PitchNode> {

  public PitchSpace(double alpha, double beta) {

    super(alpha, beta);
  }

  public PitchSpace() {

    this(Y_ALPHA, Y_BETA);
  }

  @Override
  public void init_graph() {

    for (Pitch pitch : Pitch.values()) {
      IntStream.of(2, 3, 4, 5, 6, 8)
              .mapToObj(pitch::up)
              .filter(Objects::nonNull)
              .forEach(this::addEdges);
      IntStream.of(2, 3, 4, 5, 8)
              .mapToObj(pitch::down)
              .filter(Objects::nonNull)
              .forEach(this::addEdges);
    }
  }

  @Override
  @Deprecated
  public PitchMove move(PitchNode current, double pheromone_deposit,
          double explore_chance, double... parameters) {

    System.out.println("Invalid call: PitchSpace move().");
    System.exit(-1);
    return null;
  }

  /**
   * Ants move according to the given parameters. Please be noted that the
   * outcomes would be different since version 4.0.4 with the same seeds because
   * of the changes in design.
   *
   * @param current
   * @param dominant
   * @param filter
   * @param pheromone_deposit
   * @param explore_chance
   * @param parameters
   * @return
   */
  public PitchMove move(PitchNode current, PitchNode dominant,
          Predicate<PitchPath> filter,
          double pheromone_deposit, double explore_chance,
          double... parameters) {

    var paths = this.queryByVertex(current).stream()
            .filter(filter::test)
            .collect(Collectors.toList());
    if (paths.isEmpty()) {
      return null;
    }

    var result = switch (this.getFraction_mode()) {
      case Coefficient,Power ->
        super.move(
        paths, pheromone_deposit, explore_chance);
      case Power_Multiply ->
        super.move_power_multiply(
        paths, pheromone_deposit, explore_chance);
    };

    return new PitchMove(result.isExploring(), paths, result.getSelected(), MusicThought.NULL);
  }
}
