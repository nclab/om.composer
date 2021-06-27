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
package art.cctcc.music.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptMatrix {

  private Map<Pair<Integer>, BigDecimal> values;
  public final int size;

  public CptMatrix(long[][] matrix) {

    this(matrix.length);
    IntStream.range(0, matrix.length).forEach(
            r -> IntStream.range(0, matrix.length)
                    .forEach(c -> this.set(r, c, BigDecimal.valueOf(matrix[r][c]))));
  }

  public CptMatrix(CptMatrix matrix) {

    this.size = matrix.size;
    this.values = new HashMap<>(matrix.values);
  }

  public CptMatrix(int size) {

    this.size = size;
    this.values = new HashMap<>();
  }

  public void set(int r, int c, BigDecimal value) {

    if (BigDecimal.ZERO.equals(value)) {
      values.remove(new Pair(r, c));
    } else {
      values.put(new Pair(r, c), value);
    }
  }

  BigDecimal get(int i, int j) {

    return values.getOrDefault(new Pair(i, j), BigDecimal.ZERO);
  }

  public BigDecimal toCount() {

    return this.values.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public String toString() {

    return IntStream.range(0, this.size).mapToObj(
            r -> String.format(
                    "[%s]",
                    IntStream.range(0, this.size)
                            .mapToObj(c -> get(r, c).toString())
                            .collect(Collectors.joining(", "))))
            .collect(Collectors.joining(System.lineSeparator()));
  }
}
