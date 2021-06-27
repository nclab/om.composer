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

import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.Objects;
import tech.metacontext.ocnhfa.antsomg.impl.StandardVertex;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptPitchNode extends StandardVertex {

  private CptPitch pitch;

  private static CptPitchNode empty = new CptPitchNode();

  public static CptPitchNode getEmptyNode() {

    return empty;
  }

  public CptPitchNode(CptPitch pitch) {

    super(pitch.name());
    this.pitch = pitch;
  }

  private CptPitchNode() {

    super(null);
  }

  public CptPitch getPitch() {
    return pitch;
  }

  public void setPitch(CptPitch pitch) {
    this.pitch = pitch;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.pitch.name().hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj instanceof PitchNode) {
      return Objects.equals(this.pitch.name(), ((PitchNode) obj).getName());
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return this.pitch == ((CptPitchNode) obj).pitch;
  }
}
