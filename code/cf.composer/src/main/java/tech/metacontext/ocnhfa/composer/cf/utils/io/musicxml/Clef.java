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
package tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml;

import java.util.stream.Stream;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Clef {

    Treble("G", 2, Pitch.G4),
    Bass("F", 4, Pitch.D3),
    Soprano("C", 1, Pitch.G4),
    Alto("C", 3, Pitch.C4),
    Tenor("C", 4, Pitch.A3);

    public final String sign;
    public final String line;
    public final Pitch center;

    Clef(String sign, int line, Pitch center) {

        this.sign = sign;
        this.line = String.valueOf(line);
        this.center = center;
    }

    public static Clef selector(Pitch average) {

        return Stream.of(Clef.values())
                .sorted((c1, c2)
                        -> Math.abs(Pitch.diff(average.getNode(), c1.center.getNode()))
                - Math.abs(Pitch.diff(average.getNode(), c2.center.getNode()))
                ).findFirst().get();
    }
}
