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

import art.cctcc.music.cpt.ex.ImmatureCptCfPitchSpaceException;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.graphs.y_cpt_cf.CptCfPitchSpace;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.CF_LENGTH_HIGHER;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptCalculator {

  public static List<Entry<CptCantusFirmus, BigDecimal>> cpt_score(Map<CptCantusFirmus, Double> cfs_map) {

    return cpt_count(List.copyOf(cfs_map.keySet())).stream()
            .map(e
                    -> Map.entry(
                    e.getKey(),
                    cfRating(e).multiply(BigDecimal.valueOf(cfs_map.get(e.getKey())))
                            .setScale(2, RoundingMode.HALF_UP)))
            .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toList());
  }

  public static List<Entry<CptCantusFirmus, BigDecimal[]>> cpt_count(List<CptCantusFirmus> cfs) {

    return cfs.stream()
            .parallel()
            .map(cf -> Map.entry(cf, countBothSides(cf)))
            .peek(e -> System.out.printf("%s: %s\n", e.getKey(), Arrays.toString(e.getValue())))
            .collect(Collectors.toList());
  }

  public static BigDecimal[] countBothSides(CptCantusFirmus cf) {

    var treble = new CptCfPitchSpace(cf, true);
    var bass = new CptCfPitchSpace(cf, false);
    BigDecimal treble_count, bass_count;
    try {
      treble.init_graph();
      treble_count = treble.cpt_count();
    } catch (ImmatureCptCfPitchSpaceException ex) {
      treble_count = BigDecimal.ZERO;
    }
    try {
      bass.init_graph();
      bass_count = bass.cpt_count();
    } catch (ImmatureCptCfPitchSpaceException ex) {
      bass_count = BigDecimal.ZERO;
    }
    return new BigDecimal[]{treble_count, bass_count};
  }

  public static BigDecimal cfCountRating(BigDecimal[] p) {

    return p.length == 2
            ? p[0].multiply(p[1])
                    .divide(BigDecimal.ONE.add(p[0]).add(p[1]),
                            2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
  }

  public static BigDecimal cfRating(Entry<CptCantusFirmus, BigDecimal[]> e) {

    var p0 = e.getValue()[0];
    var p1 = e.getValue()[1];
    var index = e.getKey().length() - CF_LENGTH_HIGHER;
    if (index < 1) {
      index = 1;
    }
    return BigDecimal.valueOf(Math.pow(
            cfCountRating(new BigDecimal[]{p0, p1}).doubleValue(),
            1.0 / index
    ));
  }

  public static CptMatrix pitchSpaceToMatrix(CptCfPitchSpace y) {

    var loci = y.getLoci();
    var mapper = IntStream.range(0, loci.size()).boxed()
            .flatMap(
                    i -> loci.get(i).stream()
                            .map(CptPitchPath::getTo)
                            .distinct()
                            .map(to -> Map.entry(i, to)))
            .collect(Collectors.toList());
    var size = mapper.size();
    var result = new CptMatrix(size);
    for (int r = 0; r < size; r++) {
      var from = mapper.get(r);
      for (int c = 0; c < size; c++) {
        var to = mapper.get(c);
        result.set(r, c,
                (from.getKey() == to.getKey() - 1)
                && y.queryByVertex(to.getKey(), from.getValue()).stream()
                        .map(CptPitchPath::getTo)
                        .anyMatch(to.getValue()::equals)
                ? BigDecimal.ONE : BigDecimal.ZERO);
      }
    }
    return result;
  }

  public static CptMatrix matrix_power(CptMatrix matrix, int power) {

    var result = new CptMatrix(matrix);
    for (var i = 0; i < power - 1; i++) {
      var left = new CptMatrix(result);
      result = new CptMatrix(matrix.size);
      for (var r = 0; r < result.size; r++) {
        for (var c = 0; c < result.size; c++) {
          for (int n = 0; n < result.size; n++) {
            result.set(
                    r, c,
                    result.get(r, c).add(left.get(r, n).multiply(matrix.get(n, c))));
          }
        }
      }
    }
    return result;
  }
}
