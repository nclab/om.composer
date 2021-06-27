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

import art.cctcc.music.cpt.model.enums.CptPitch;
import java.util.List;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.composer.cf.model.devices.CantusFirmus;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptCantusFirmus extends CptMelody {

  public CptCantusFirmus(String id) {

    super(id);
  }

  /**
   * Constructor of CptCantusFirmus with legacy CantusFirmus object.
   *
   * @param id
   * @param cf legacy CantusFirmus object from cf.composer.
   */
  public CptCantusFirmus(String id, CantusFirmus cf) {

    this(id, cf.getMelody().stream()
            .map(PitchNode::getName)
            .map(CptPitch::valueOf)
            .map(CptPitch::getNode)
            .collect(Collectors.toList()));
  }

  /**
   * Constructor of CptCantusFirmus with melody.
   *
   * @param id
   * @param melody List of CptPitchNode or CptPitch
   */
  public CptCantusFirmus(String id, List melody) {

    super(id, melody);
  }
}
