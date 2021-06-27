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
package tech.metacontext.ocnhfa.composer.cf.model.enums;

import java.util.function.Predicate;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedMusicNodeException;
import tech.metacontext.ocnhfa.composer.cf.ex.UnexpectedMusicThoughtException;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.x.MusicNode;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum MusicThought {

    Directional_Conjunct,
    Directional_Disjunct,
    Complemental_ShortTerm,
    Complemental_LongTerm,
    NULL;

    public static final MusicNode START = new MusicNode("Start");
    public static final MusicNode DIRECTIONAL = new MusicNode("Directional");
    public static final MusicNode COMPLEMENTAL = new MusicNode("Complemental");
    public static final MusicNode CONJUNCT = new MusicNode("Conjunct");
    public static final MusicNode DISJUNCT = new MusicNode("Disjunct");
    public static final MusicNode SHORTTERM = new MusicNode("ShortTerm");
    public static final MusicNode LONGTERM = new MusicNode("LongTerm");

    public static MusicNode getNode(String name) {

        return switch (name) {
            case "Start"->
                START;
            case "Directional"->
                DIRECTIONAL;
            case "Complemental"->
                COMPLEMENTAL;
            case "Conjunct"->
                CONJUNCT;
            case "Disjunct"->
                DISJUNCT;
            case "ShortTerm"->
                SHORTTERM;
            case "LongTerm"->
                LONGTERM;
            default->
                throw new UnexpectedMusicThoughtException(name);
        };
    }

    public Predicate<PitchPath> getPredicate(MusicThread thread) {

        return path -> thread.getCf().length() > 1 ? switch (this) {
            case Directional_Conjunct:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) == 2)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) == -2);
            case Directional_Disjunct:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) > 2)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) < -2);
            case Complemental_LongTerm:
                if (thread.lastPitchLevel() != 0) {
                    yield (thread.lastPitchLevel() > 0 && Pitch.diff(path) < 0)
                    || (thread.lastPitchLevel() < 0 && Pitch.diff(path) > 0);
                }
            case Complemental_ShortTerm:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) < 0)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) > 0);
            default:
                yield true;
        } : switch (this) {
            case Directional_Conjunct->
                Pitch.diff(path) == 2 || Pitch.diff(path) == -2;
            case Directional_Disjunct->
                Pitch.diff(path) > 2 || Pitch.diff(path) < -2;
            default->
                true;
        };
    }

    public static MusicThought getInstance(MusicNode node1, MusicNode node2)
            throws UnexpectedMusicNodeException {

        try {
            return MusicThought.valueOf(node1.getName() + "_" + node2.getName());
        } catch (Exception e) {
            throw new UnexpectedMusicNodeException(node1, node2);
        }
    }
}
