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
package art.cctcc.music.cpt.model;

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode;
import java.util.List;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptCounterpoint extends CptMelody {

  private CptCantusFirmus cf;

  public CptCounterpoint(CptCantusFirmus cf) {

    super(cf.getId());
    this.cf = cf;
  }

  public CptCounterpoint(String id, List<CptPitchNode> melody) {

    super(id, melody);
  }

  public CptCantusFirmus getCf() {

    return cf;
  }

  public void setCf(CptCantusFirmus cf) {

    this.cf = cf;
  }

  @Override
  public CptEcclesiasticalMode getMode() {

    return this.cf.getMode();
  }
}
