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
package tech.metacontext.ocnhfa.composer.cf.model.constraints;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.*;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicThreadConstraint implements Predicate<MusicThread> {

  @Override
  public boolean test(MusicThread thread) {

    return inRange(thread)
            && properRepetition(thread)
            && properLeaps(thread)
            && properLength(thread);
  }

  public static boolean inRange(MusicThread thread) {

    return thread.currentRange() <= 8 && thread.currentRange() >= 4;
  }

  public static boolean properRepetition(MusicThread thread) {

    var map = new HashMap<PitchNode, Integer>();
    var cf = thread.getCf().getMelody();
    for (int i = 0; i < thread.getCf().length(); i++) {
      var node = cf.get(i);
      if (i == 0 || !node.equals(cf.get(i - 1))) {
        var count = map.getOrDefault(node, 0);
        map.put(node, count + 3);
        if (map.values().stream().anyMatch(v -> v > 4)) {
          return false;
        }
      }
      map.replaceAll((n, v) -> v > 0 ? v - 1 : 0);
    }
    return true;
  }

  public static boolean properLeaps(MusicThread thread) {

    int disjunct_counter = 0;
    int avoided_leap_counter = 0;
    var melody = thread.getCf().getMelody();
    var melody_array = IntStream.range(1, melody.size())
            .map(i -> melody.get(i).getPitch().ordinal() - melody.get(i - 1).getPitch().ordinal())
            .toArray();
    for (int i = 0; i < melody_array.length; i++) {
      var current = melody_array[i];
      if (Math.abs(current) > 2) {
        disjunct_counter++;
      }
      if (current > 1 && i > 0) {
        var previous = melody_array[i - 1];
        if (previous > 0
                && (current == 2 && current > previous || current > 2 && current >= previous)) {
          avoided_leap_counter++;
        }
      } else if (current < -1 && i < melody_array.length - 1) {
        var next = melody_array[i + 1];
        if (next < 0
                && (current == -2 && current < next || current < -2 && current <= next)) {
          avoided_leap_counter++;
        }
      }
    }
    return avoided_leap_counter + disjunct_counter > 0
            && avoided_leap_counter + disjunct_counter <= 2 * (melody.size() / CF_LENGTH_LOWER);
  }

  public static boolean properLength(MusicThread thread) {

    return thread.getCf().length() >= CF_LENGTH_LOWER
            && thread.getCf().length() <= CF_LENGTH_HIGHER;
  }
}
