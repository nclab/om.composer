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
package tech.metacontext.ocnhfa.composer.cf.model.devices;

import java.util.List;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Cadence {

    private final PitchNode finalis;

    private final List<PitchNode> formula;

    public Cadence(PitchNode finalis) {

        this.finalis = finalis;
        this.formula = List.of(finalis.getPitch().up(2).getTo(), this.finalis);
    }

    public boolean isLinked(PitchNode pitch) {

        return formula.get(0).equals(pitch);
    }

    @Override
    public String toString() {

        return "Cadence{" + "formula=" + formula + '}';
    }

    /*
     * Default getters and setters.
     */
    public List<PitchNode> getFormula() {

        return formula;
    }

    public PitchNode getFinalis() {

        return finalis;
    }
}
