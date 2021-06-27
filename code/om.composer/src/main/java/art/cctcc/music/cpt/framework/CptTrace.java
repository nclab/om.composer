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
package art.cctcc.music.cpt.framework;

import art.cctcc.music.cpt.graphs.x.CptMusicMove;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchMove;
import java.util.Objects;
import tech.metacontext.ocnhfa.antsomg.model.Trace;
import tech.metacontext.ocnhfa.antsomg.model.Vertex;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptTrace implements Trace {

  private CptMusicMove x;
  private CptPitchMove y;

  public CptTrace(CptMusicMove x_move, CptPitchMove y_move) {

    this.x = x_move;
    this.y = y_move;
  }

  @Override
  public Vertex getDimension(String dimension) {

    return switch (dimension) {
      case "x0": yield this.x.getMoves()[0].getSelected().getTo();
      case "x1": yield this.x.getMoves()[1].getSelected().getTo();
      case "y": yield this.y.getSelected().getTo();
      default: yield null;
    };
  }

  @Override
  public String toString() {

    var result = "CptMusicTrace " + (Objects.isNull(x)
            ? String.format("[y=%s]", getY().getSelected())
            : String.format("[x=%s, y=%s]", getX().getMusicThought(), getY().getSelected()));
    return result;
  }

  public CptMusicMove getX() {

    return x;
  }

  public CptPitchMove getY() {

    return y;
  }
}
