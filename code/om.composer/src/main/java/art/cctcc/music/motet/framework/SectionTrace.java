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
package art.cctcc.music.motet.framework;

import art.cctcc.music.motet.graphs.SectionPath;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.model.Trace;
import tech.metacontext.ocnhfa.antsomg.model.Vertex;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SectionTrace implements Trace {

  private final StandardMove<SectionPath> move;

  public SectionTrace(StandardMove<SectionPath> move) {

    this.move = move;
  }

  @Override
  public Vertex getDimension(String dimension) {

    return this.move.getSelected().getTo();
  }

  public StandardMove<SectionPath> getMove() {

    return move;
  }

  @Override
  public String toString() {

    return "SectionTrace{" + "move=" + move + '}';
  }

}
