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

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.antsomg.impl.StandardParameters;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Mutator;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.devices.CantusFirmus;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchMove;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchPath;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchSpace;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CF_Mutator implements Mutator<MusicThread> {

   public static PitchSpace y = new PitchSpace();

   static {
      y.init_graph();
   }

   @Override
   public MusicThread mutate(MusicThread t0) {

      var cf = t0.getCf();
      var new_history = new LinkedList<PitchMove>();
      var locus = StandardParameters.getRandom().nextInt(cf.length() - 3) + 1;
      boolean searching = true;
      for (int i = 0; i < cf.length(); i++) {
         var move = cf.getHistory().get(i);
         if (i < locus || !searching) {
            new_history.add(move);
         } else {
            // i >= loci && searching
            var p1 = move.getSelected().getFrom();
            var p2 = move.getSelected().getTo();
            var p3 = cf.getMelody().get(i + 1); //C = E4
            // To find P1 -> X -> P3, and X is not P2.
            var candidates = y.queryByVertex(p1).stream()
                    // P1 -> X and X is not P2
                    .filter(path -> !path.getTo().equals(p2))
                    // path = P1 -> X
                    // path0: X -> Y and Y is P3
                    .map(path -> new Pair<PitchPath>(path, y.queryByVertex(path.getTo()).stream().filter(path0 -> path0.getTo().equals(p3)).findFirst().orElse(null)))
                    .filter(pair -> Objects.nonNull(pair.e2()))
                    .collect(Collectors.toList());
            if (!candidates.isEmpty()) {
               searching = false;
               var selected = candidates.get(getRandom().nextInt(candidates.size()));
               new_history.add(new PitchMove(move.isExploring(),
                               y.queryByVertex(p1),
                               selected.e1(), move.getMt()));
               new_history.add(new PitchMove(false,
                               y.queryByVertex(selected.e1().getTo()),
                               selected.e2(), move.getMt()));
               i++;
            } else {
               if (i >= cf.length() - 3) {
                  searching = false;
               }
               new_history.add(move);
            }
         }
      }
      var mutated = new CantusFirmus(cf.getEcclesiastical_Mode(), new_history);
      return new MusicThread(mutated);
   }

}
