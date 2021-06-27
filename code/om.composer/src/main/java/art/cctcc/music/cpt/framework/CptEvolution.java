/*
 * Copyright 2021 Administrator.
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
package art.cctcc.music.cpt.framework;

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchPath;
import art.cctcc.music.cpt.model.CptMelody;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptEvolution {

  private final CptComposer composer;
  private final double threshold;
  private final int generation;
  private final List<Double> evals;

  public CptEvolution(CptComposer composer, double threshold, int generation) {

    this.composer = composer;
    this.threshold = threshold;
    this.generation = generation;
    this.evals = new ArrayList<>();
  }

  public final void run() {

    this.evals.add(composer.getAverageEval());
    var generation_count = 0;
    boolean complete;
    System.out.printf(" (%.2f", this.evals.get(0));
    do {
      var crossover_children = getCrossoverChildren();

      var mutants = Stream.generate(this::tournamentSelection_k2)
              .distinct()
              .limit((int) (composer.getPopulation() * MUTATION_RATE))
              .map(this::mutation)
              .toArray(CptThread[]::new);

      var generated = Stream.of(crossover_children, mutants)
              .flatMap(Stream::of)
              .toArray(CptThread[]::new);

      composer.insert(generated);

      var eval = composer.getAverageEval();
      this.evals.add(eval);
      if (generation_count++ % this.generation / 10 == 0) {
        System.out.print(".");
      }
      complete = eval >= threshold || generation_count >= this.generation;
    } while (!complete);
    System.out.printf("%.2f)", composer.getAverageEval());
  }

  public CptThread[] getCrossoverChildren() {

    List<CptThread> result = new ArrayList<>();
    do {
      var parent_domestic = this.tournamentSelection_k2();
      var parent_external = this.composer.generate();
      this.composer.developThread(parent_external);
      var children = this.crossover(parent_domestic, parent_external);
      if (children != null) {
        result.addAll(List.of(children));
      }
    } while (result.size() < composer.getPopulation() * CROSSOVER_RATE);
    return result.toArray(CptThread[]::new);
  }

  public CptThread[] crossover(CptThread... threads) {

    var t0 = threads[0].getCpt().getMelody();
    var t1 = threads[1].getCpt().getMelody();
    var loci = IntStream.range(1, t0.size() - 4)
            .filter(i -> Objects.equals(t0.get(i), t1.get(i)))
            .toArray();
    if (loci.length == 0 || loci.length == t0.size() - 5) {
      return null;
    }
    var locus = loci[getRandom().nextInt(loci.length)];
    assert t0.get(locus).equals(t1.get(locus));
    List<CptPitchNode> c0 = new ArrayList<>();
    List<CptPitchNode> c1 = new ArrayList<>();
    IntStream.range(0, t0.size())
            .forEach(i -> {
              if (i < locus) {
                c0.add(t0.get(i));
                c1.add(t1.get(i));
              } else {
                c0.add(t1.get(i));
                c1.add(t0.get(i));
              }
            });
    return new CptThread[]{
      new CptThread(composer.getCf(), c0),
      new CptThread(composer.getCf(), c1)};
  }

  public CptThread mutation(CptThread thread) {

    var t0 = thread.getCpt();
    final var locus = Stream.generate(() -> getRandom().nextInt(t0.length() - 4))
            .filter(i -> composer.getY().queryByVertex(i, t0.getNote(i - 1)).size() > 1)
            .findFirst().get();
    final var candidate = composer.getY().queryByVertex(locus, t0.getNote(locus - 1));
    final var current = thread.getCpt().getNote(locus);
    final var mutated_locus = Stream.generate(() -> getRandom().nextInt(candidate.size()))
            .map(i -> {
              var it = candidate.iterator();
              for (var j = 0; j < i; j++) {
                it.next();
              }
              return it.next();
            })
            .filter(path -> !path.getTo().equals(current))
            .findFirst().get();
    assert !Objects.equals(current, mutated_locus);
    CptPitchNode[] mutant = mutate(
            IntStream.range(0, locus)
                    .mapToObj(i -> t0.getNote(i))
                    .toArray(CptPitchNode[]::new),
            List.of(mutated_locus), t0);
    var result = Stream.of(mutant, IntStream.range(mutant.length, t0.length()).mapToObj(t0::getNote).toArray(CptPitchNode[]::new))
            .flatMap(Stream::of)
            .toArray(CptPitchNode[]::new);
    var result_thread = new CptThread(composer.getCf(), List.of(result));
    return result_thread;
  }

  private CptPitchNode[] mutate(CptPitchNode[] mutant0, List<CptPitchPath> list0, CptMelody t0) {

    for (CptPitchPath path : list0) {
      var pitch = path.getTo();
      var mutant1 = append(mutant0, pitch);
      if (mutant1.length == t0.length()) {
        return mutant1;
      }
      var list1 = composer.getY().queryByVertex(mutant1.length, pitch);
      var merge_point = list1.stream()
              .map(CptPitchPath::getTo)
              .filter(t0.getNote(mutant1.length)::equals)
              .findAny().orElse(null);
      return (merge_point != null)
              ? append(mutant1, merge_point)
              : mutate(mutant1, list1, t0);
    }
    return null;
  }

  private static <E> E[] append(E[] a0, E e) {

    E[] a = Arrays.copyOf(a0, a0.length + 1);
    a[a0.length] = e;
    return a;
  }

  public CptThread tournamentSelection_k2() {

    return tournamentSelection(composer.getAnts(), 2);
  }

  public static <T> T tournamentSelection(List<T> pool, int num_selected) {

    var selected = IntStream.generate(() -> getRandom().nextInt(pool.size()))
            .distinct()
            .limit(num_selected)
            .min().getAsInt();
    return pool.get(selected);
  }

  public CptComposer getComposer() {

    return composer;
  }

  public int getGeneration() {

    return generation;
  }

  public List<Double> getEvals() {

    return evals;
  }
}
