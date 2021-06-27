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
package tech.metacontext.ocnhfa.composer.cf.model;

import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.model.Trace;
import tech.metacontext.ocnhfa.antsomg.model.Vertex;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicPath;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicTrace implements Trace {

    MusicNode x;
    PitchNode y;

    public MusicTrace(MusicNode x, PitchNode y) {

        this.x = x;
        this.y = y;
    }

    public MusicTrace(StandardMove<MusicPath> x_move, PitchNode y) {

        this.x = x_move.getSelected().getTo();
        this.y = y;
    }

    @Override
    public Vertex getDimension(String dimension) {

        return switch (dimension) {
            case "x"->
                this.x;
            case "y"->
                this.y;
            default->
                null;
        };
    }

    public MusicNode getX() {

        return this.x;
    }

    public PitchNode getY() {

        return this.y;
    }

    @Override
    public String toString() {

        return String.format("[%s, %s]", x.getName(), y.getPitch());
    }

}
