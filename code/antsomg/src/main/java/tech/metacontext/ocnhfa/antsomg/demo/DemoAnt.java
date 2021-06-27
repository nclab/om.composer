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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import tech.metacontext.ocnhfa.antsomg.demo.x.Edge_X;
import tech.metacontext.ocnhfa.antsomg.demo.x.Vertex_X;
import tech.metacontext.ocnhfa.antsomg.demo.y.Edge_Y;
import tech.metacontext.ocnhfa.antsomg.demo.y.Vertex_Y;
import tech.metacontext.ocnhfa.antsomg.demo.z.Edge_Z;
import tech.metacontext.ocnhfa.antsomg.demo.z.Vertex_Z;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.model.Ant;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class DemoAnt implements Ant<DemoTrace> {

    DemoTrace currentTrace;
    List<DemoTrace> route;
    private boolean completed;

    public DemoAnt(Vertex_X x, Vertex_Y y, Vertex_Z z) {

        this.currentTrace = new DemoTrace(
                new StandardMove<>(new Edge_X(x)),
                new StandardMove<>(new Edge_Y(y)),
                new StandardMove<>(new Edge_Z(z)));
        this.route = new ArrayList<>();
    }

    @Override
    public List<DemoTrace> getRoute() {

        return this.route;
    }

    @Override
    public void addCurrentTraceToRoute() {

        this.route.add(this.currentTrace);
    }

    @Override
    public DemoTrace getCurrentTrace() {

        return this.currentTrace;
    }

    @Override
    public void setCurrentTrace(DemoTrace trace) {

        if (Objects.nonNull(this.currentTrace)) {
            this.addCurrentTraceToRoute();
        }
        this.currentTrace = trace;
    }

    public boolean isCompleted() {

        return completed;
    }

    public void setCompleted(boolean completed) {

        this.completed = completed;
    }

}
