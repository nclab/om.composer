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
package tech.metacontext.ocnhfa.antsomg.demo;

import tech.metacontext.ocnhfa.antsomg.demo.x.Edge_X;
import tech.metacontext.ocnhfa.antsomg.demo.y.Edge_Y;
import tech.metacontext.ocnhfa.antsomg.demo.z.Edge_Z;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.model.Trace;
import tech.metacontext.ocnhfa.antsomg.model.Vertex;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class DemoTrace implements Trace {

    private final StandardMove<Edge_X> x;
    private final StandardMove<Edge_Y> y;
    private final StandardMove<Edge_Z> z;

    public DemoTrace(
            StandardMove<Edge_X> x,
            StandardMove<Edge_Y> y,
            StandardMove<Edge_Z> z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vertex getDimension(String dimension) {

        return switch (dimension) {
            case "x"->
                this.x.getSelected().getTo();
            case "y"->
                this.y.getSelected().getTo();
            case "z"->
                this.z.getSelected().getTo();
            default->
                null;
        };
    }

    public StandardMove<Edge_X> getX() {

        return this.x;
    }

    public StandardMove<Edge_Y> getY() {

        return this.y;
    }

    public StandardMove<Edge_Z> getZ() {

        return this.z;
    }

    @Override
    public String toString() {

        return String.format("DemoTrace{x=%s, y=%s, z=%s}",
                x.getSelected().getTo().getName(),
                y.getSelected().getTo().getName(),
                z.getSelected().getTo().getName());
    }

}
