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
package tech.metacontext.ocnhfa.antsomg.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tech.metacontext.ocnhfa.antsomg.model.Move;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <E>
 */
public class StandardMove<E extends StandardEdge<? extends StandardVertex>>
        implements Move<E> {

    private final boolean exploring;
    private final E selected;
    private final Map<E, Double> pheromoneRecords;

    public static <E extends StandardEdge<? extends StandardVertex>>
            StandardMove<E> getInstance(boolean exploring, List<E> edges, E selected) {

        return new StandardMove<>(exploring, edges, selected);
    }

    public StandardMove(boolean exploring, List<E> edges, E selected) {

        this(exploring, selected);
        edges.stream()
                .forEach(e -> this.pheromoneRecords.put(e, e.getPheromoneTrail()));
    }

    public StandardMove(boolean exploring, E selected) {

        this.exploring = exploring;
        this.selected = selected;
        this.pheromoneRecords = new HashMap<>();
    }

    public StandardMove(E selected) {

        this(false, selected);
    }

    public Double getPheromoneTrail(E edge) {

        return this.pheromoneRecords.get(edge);
    }

    @Override
    public boolean isExploring() {

        return this.exploring;
    }

    @Override
    public E getSelected() {

        return this.selected;
    }

    public Map<E, Double> getPheromoneRecords() {

        return this.pheromoneRecords;
    }

}
