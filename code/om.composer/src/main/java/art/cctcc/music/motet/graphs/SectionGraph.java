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
package art.cctcc.music.motet.graphs;

import static art.cctcc.music.Parameters.*;
import static art.cctcc.music.motet.model.enums.SectionType.*;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SectionGraph extends StandardGraph<SectionPath, SectionNode> {

  public SectionGraph(double alpha, double beta) {

    super(alpha, beta);
  }

  public SectionGraph() {

    super();
  }

  @Override
  public void init_graph() {

    var cf = CF.node;
    var lower_cpt = CPT_BASS.node;
    var upper_cpt = CPT_TREBLE.node;
    var finish = FINISH.node;

    this.setStart(cf);
    var cf_lower = new SectionPath(cf, lower_cpt, COST_STANDARD);
    var lower_cf = cf_lower.getReverse(COST_CPT_TO_CF);
    var cf_upper = new SectionPath(cf, upper_cpt, COST_STANDARD);
    var upper_cf = cf_upper.getReverse(COST_CPT_TO_CF);
    var upper_lower = new SectionPath(upper_cpt, lower_cpt, COST_STANDARD);
    var lower_upper = upper_lower.getReverse();
    var upper_loop = new SectionPath(upper_cpt, upper_cpt, COST_STANDARD);
    var lower_loop = new SectionPath(lower_cpt, lower_cpt, COST_STANDARD);
    var upper_finish = new SectionPath(upper_cpt, finish, COST_CPT_TO_FINISH);
    var lower_finish = new SectionPath(lower_cpt, finish, COST_CPT_TO_FINISH);

    this.addEdges(
            cf_lower, lower_cf,
            cf_upper, upper_cf,
            upper_lower, lower_upper,
            upper_loop, lower_loop,
            upper_finish, lower_finish
    );
  }

  @Override
  public String asGraphviz() {

    var developed = getEdges().stream()
            .map(SectionPath::getPheromoneTrail)
            .anyMatch(ph -> ph > 0.0);

    return String.format("""
                         digraph %s {
                         \trankdir=LR;
                         \tnodesep=0.6;
                         %s
                         \t{
                         \t\trank=same; %s; %s;
                         \t}
                         }""",
            this.getClass().getSimpleName(),
            getEdges().stream().map(
                    path -> new Object[]{path.getFrom().getName(),
                      path.getTo().getName(), "label",
                      path.getCost(), path.getPheromoneTrail()}
            ).peek(o -> {
              if (CPT_TREBLE.name().equals(o[0]) && CPT_BASS.name().equals(o[1])) {
                o[0] += ":sw";
                o[1] += ":nw";
                o[2] = "xlabel";
              } else if (CPT_BASS.name().equals(o[0]) && CPT_TREBLE.name().equals(o[1])) {
                o[0] += ":ne";
                o[1] += ":se";
              } else if (CPT_TREBLE.name().equals(o[0]) && CPT_TREBLE.name().equals(o[1])) {
                o[0] += ":n";
                o[1] += ":n ";
              } else if (CPT_BASS.name().equals(o[0]) && CPT_BASS.name().equals(o[1])) {
                o[0] += ":s";
                o[1] += ":s";
              }
            }).map(
                    o -> developed
                            ? String.format("\t%s -> %s [%s=<c=%.1f,p=%.3f>, fontsize=10];", o)
                            : String.format("\t%s -> %s [%s=<c=%.1f>, fontsize=10];", o)
            ).collect(Collectors.joining("\n")),
            CPT_TREBLE.name(),
            CPT_BASS.name());
  }
}
