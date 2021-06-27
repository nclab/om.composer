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

import art.cctcc.music.cpt.framework.CptThread;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.function.Predicate;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptMusicMove {

  private StandardMove<MusicPath>[] moves;

  public CptMusicMove(StandardMove<MusicPath> move0, StandardMove<MusicPath> move1) {

    this.moves = new StandardMove[]{move0, move1};
  }

  public CptMusicMove(MusicNode move0, MusicNode move1) {

    this(new StandardMove<MusicPath>(new MusicPath(MusicThought.START, move0, 0.0)),
            new StandardMove<MusicPath>(new MusicPath(move0, move1, 0.0)));

  }

  public StandardMove<MusicPath>[] getMoves() {

    return moves;
  }

  public MusicThought getMusicThought() {

    return MusicThought.getInstance(
            this.moves[0].getSelected().getTo(),
            this.moves[1].getSelected().getTo());
  }

  public Predicate<CptPitchPath> getPredicate(CptThread thread) {

    return (CptPitchPath path) -> {
      if (thread.getRoute().isEmpty()) {
        return switch (this.getMusicThought()) {
          case Directional_Conjunct: yield path.absDiff() <= 2;
          case Directional_Disjunct: yield path.absDiff() > 2;
          default: yield true;
        };
      } else {
        return switch (this.getMusicThought()) {
          case Directional_Conjunct ->
            (thread.lastPitchDirection() >= 0 && CptPitch.diff(path) <= 2)
            || (thread.lastPitchDirection() <= 0 && CptPitch.diff(path) >= -2);
          case Directional_Disjunct ->
            (thread.lastPitchDirection() >= 0 && CptPitch.diff(path) > 2)
            || (thread.lastPitchDirection() <= 0 && CptPitch.diff(path) < -2);
          case Complemental_LongTerm ->
            (thread.lastPitchLevel() == 0)
            ? (thread.lastPitchDirection() >= 0 && CptPitch.diff(path) < 0)
            || (thread.lastPitchDirection() <= 0 && CptPitch.diff(path) > 0)
            : (thread.lastPitchLevel() > 0 && CptPitch.diff(path) < 0)
            || (thread.lastPitchLevel() < 0 && CptPitch.diff(path) > 0);
          case Complemental_ShortTerm ->
            (thread.lastPitchDirection() >= 0 && CptPitch.diff(path) < 0)
            || (thread.lastPitchDirection() <= 0 && CptPitch.diff(path) > 0);
          default ->
            true;
        };
      }
    };
  }
}
