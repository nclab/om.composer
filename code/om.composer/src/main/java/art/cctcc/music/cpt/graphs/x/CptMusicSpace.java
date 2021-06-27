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
package art.cctcc.music.cpt.graphs.x;

import static art.cctcc.music.Parameters.ALPHA;
import static art.cctcc.music.Parameters.BETA;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicPath;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicSpace;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptMusicSpace extends MusicSpace {

  public CptMusicSpace(double alpha, double beta) {

    super(alpha, beta);
  }

  public CptMusicSpace() {

    this(ALPHA, BETA);
  }

  public CptMusicSpace duplicate() {

    var new_graph = new CptMusicSpace(this.alpha, this.beta);
    new_graph.init_graph();
    this.getEdges().forEach(path0 -> {
      var new_path = new_graph.queryByVertex(path0.getFrom()).stream()
              .filter(path -> Objects.equals(path.getTo(), path0.getTo()))
              .findFirst().get();
      new_path.setPheromoneTrail(path0.getPheromoneTrail());
    });
    return new_graph;
  }

  @Override
  @Deprecated
  public StandardMove<MusicPath> move(MusicNode current, double pheromone_deposit, double explore_chance, double... parameters) {

    System.out.println("Invalid call: CptMusicSpace move().");
    System.exit(-1);
    return null;
  }

  public StandardMove<MusicPath> getMove(MusicNode current, double explore_chance) {

    var paths = this.queryByVertex(current);
    var fractions = new ArrayList<Double>();
    var sum = paths.stream()
            .mapToDouble(this::getFraction)
            .peek(fractions::add)
            .sum();
    var r = new AtomicReference<Double>(getRandom().nextDouble() * sum);
    var isExploring = getRandom().nextDouble() < explore_chance;
    var selected = isExploring
            ? paths.get(getRandom().nextInt(paths.size()))
            : IntStream.range(0, paths.size())
                    .filter(i -> r.getAndSet(r.get() - fractions.get(i)) < fractions.get(i))
                    .mapToObj(paths::get)
                    .findFirst().get();

    return StandardMove.getInstance(isExploring, paths, selected);
  }

  public void move(CptMusicMove move, double pheromone_deposit) {

    move.getMoves()[0].getSelected().addPheromoneDeposit(pheromone_deposit);
    move.getMoves()[1].getSelected().addPheromoneDeposit(pheromone_deposit);
  }
}
