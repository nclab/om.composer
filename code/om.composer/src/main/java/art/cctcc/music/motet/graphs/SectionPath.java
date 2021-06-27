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

import tech.metacontext.ocnhfa.antsomg.impl.StandardEdge;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SectionPath extends StandardEdge<SectionNode> {

  public SectionPath(SectionNode from, SectionNode to, double cost) {

    super(from, to, cost);
  }

  @Override
  public SectionPath getReverse(double cost) {

    return new SectionPath(this.getTo(), this.getFrom(), cost);
  }

  @Override
  public SectionPath getReverse() {

    return this.getReverse(this.getCost());
  }
}
