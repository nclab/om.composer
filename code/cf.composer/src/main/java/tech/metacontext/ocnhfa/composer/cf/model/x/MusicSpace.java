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
package tech.metacontext.ocnhfa.composer.cf.model.x;

import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.*;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicSpace extends StandardGraph<MusicPath, MusicNode> {

  public MusicSpace(double alpha, double beta) {

    super(alpha, beta);
  }

  public MusicSpace() {

    this(X_ALPHA, X_BETA);
  }

  @Override
  public void init_graph() {

    this.setStart(MusicThought.START);
    /*
         * Level 1: Directional, Complemental.
     */
    var directional = new MusicPath(this.getStart(), MusicThought.DIRECTIONAL, DEFAULT_COST / 2.0);
    var complemental = new MusicPath(this.getStart(), MusicThought.COMPLEMENTAL, DEFAULT_COST);
    this.addEdges(directional, complemental);
    /*
         * Level 2-1: Directional: Upward, Downward.
     */
    var conjunt = new MusicPath(directional, MusicThought.CONJUNCT, DEFAULT_COST / DOMINANT_ATTRACTION_FACTOR);
    var disjunct = new MusicPath(directional, MusicThought.DISJUNCT, DEFAULT_COST);
    var conjunct_to_start = new MusicPath(conjunt, this.getStart(), DEFAULT_COST);
    var disjunct_to_start = new MusicPath(disjunct, this.getStart(), DEFAULT_COST);
    this.addEdges(conjunt, disjunct, conjunct_to_start, disjunct_to_start);
    /*
         * Level 2-2: Complemental: ShortTerm, LongTerm.
     */
    var short_term = new MusicPath(complemental, MusicThought.SHORTTERM, DEFAULT_COST);
    var long_term = new MusicPath(complemental, MusicThought.LONGTERM, DEFAULT_COST);
    var short_term_to_start = new MusicPath(short_term, this.getStart(), DEFAULT_COST);
    var long_term_to_start = new MusicPath(long_term, this.getStart(), DEFAULT_COST);
    this.addEdges(short_term, long_term, short_term_to_start, long_term_to_start);
    //
  }

}
