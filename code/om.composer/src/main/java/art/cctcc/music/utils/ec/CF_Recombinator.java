/*
 * Copyright 2020 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music.utils.ec;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardParameters;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Recombinator;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.devices.CantusFirmus;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CF_Recombinator implements Recombinator<MusicThread> {

    @Override
    public MusicThread recombine(Pair<MusicThread> ts) {

        var cf1 = ts.e1().getCf();
        var cf2 = ts.e2().getCf();

        var loci = IntStream.range(2, Integer.min(cf1.length(), cf2.length()) - 4)
                .filter(i -> cf1.getMelody().get(i).equals(cf2.getMelody().get(i)))
                .boxed()
                .collect(Collectors.toList());

        if (loci.isEmpty()) {
            return null;
        }

        var locus = loci.get(StandardParameters.getRandom().nextInt(loci.size()));

        var new_cf1 = new ArrayList<>(cf1.getHistory().subList(0, locus + 1));
        new_cf1.addAll(cf2.getHistory().subList(locus + 1, cf2.length()));

        var new_cf2 = new ArrayList<>(cf2.getHistory().subList(0, locus + 1));
        new_cf2.addAll(cf1.getHistory().subList(locus + 1, cf1.length()));

        var select_first = StandardParameters.getRandom().nextBoolean();
        var child = select_first ? new_cf1 : new_cf2;

        var cf = new CantusFirmus(cf1.getEcclesiastical_Mode(), child);

        return new MusicThread(cf);
    }
}
