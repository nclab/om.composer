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
package tech.metacontext.ocnhfa.composer.cf.model.x;

import tech.metacontext.ocnhfa.antsomg.impl.StandardEdge;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicPath extends StandardEdge<MusicNode> {

    /**
     *
     * @param from
     * @param to
     * @param cost
     */
    public MusicPath(MusicNode from, MusicNode to, double cost) {

        super(from, to, cost);
    }

    public MusicPath(MusicPath path, MusicNode to, double cost) {

        this(path.getTo(), to, cost);
    }

    @Override
    public String toString() {

        return String.format("MusicPath{from=%s, to=%s}(cost=%f, pheromone=%f)",
                this.getFrom(), this.getTo(), this.getCost(), this.getPheromoneTrail());
    }
}
