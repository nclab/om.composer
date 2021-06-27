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
package tech.metacontext.ocnhfa.composer.cf.model.y;

import java.util.List;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PitchMove extends StandardMove<PitchPath> {

    private MusicThought mt;

    public PitchMove(boolean exploring, List<PitchPath> edges, PitchPath selected, MusicThought mt) {

        super(exploring, edges, selected);
        this.mt = mt;
    }

    @Override
    public String toString() {

        var ph = this.getPheromoneRecords().entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(entry -> String.format("->%s(%.3f)", entry.getKey().getTo(), entry.getValue()))
                .collect(Collectors.joining(";"));
        return String.format("-> %s { Mt=%s, Explore=%5b, Pheromone=[%s] }",
                this.getSelected().getTo(),
                this.getMt(),
                this.isExploring(),
                ph);
    }

    public MusicThought getMt() {
        return mt;
    }

    public void setMt(MusicThought mt) {
        this.mt = mt;
    }

}
