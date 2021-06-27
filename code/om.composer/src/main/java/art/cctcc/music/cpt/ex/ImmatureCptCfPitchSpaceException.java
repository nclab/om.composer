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
package art.cctcc.music.cpt.ex;

import art.cctcc.music.cpt.model.CptCantusFirmus;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ImmatureCptCfPitchSpaceException extends RuntimeException {

  public final CptCantusFirmus cf;
  public final boolean treble;
  public final String graph;

  public ImmatureCptCfPitchSpaceException(CptCantusFirmus cf, boolean treble, String graph) {

    this.cf = cf;
    this.treble = treble;
    this.graph = graph;
  }

  @Override
  public String toString() {

    return String.format("ImmatureCptCfPitchSpaceException{cf=%s (%s)}",
             cf, treble ? "treble" : "bass");
  }
}
