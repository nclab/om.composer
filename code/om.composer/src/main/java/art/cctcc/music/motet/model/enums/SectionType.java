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
package art.cctcc.music.motet.model.enums;

import art.cctcc.music.motet.graphs.SectionNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum SectionType {

  CF("*CF"), CPT_TREBLE("Tre"), CPT_BASS("Bas"), FINISH("Fin");

  public SectionNode node;
  private final String abbr;

  SectionType(String abbr) {

    this.node = new SectionNode(this);
    this.abbr = abbr;
  }

  @Override
  public String toString() {

    return this.abbr;
  }
}
