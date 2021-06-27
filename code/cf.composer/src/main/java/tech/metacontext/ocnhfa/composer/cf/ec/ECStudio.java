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
package tech.metacontext.ocnhfa.composer.cf.ec;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;
import static tech.metacontext.ocnhfa.composer.cf.ec.ECOperation.mutation;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Evaluator;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Mutator;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Recombinator;
import tech.metacontext.ocnhfa.composer.cf.ex.NoQualifiedThreadsSelectedException;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.Parameters;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.*;
import tech.metacontext.ocnhfa.composer.cf.model.Studio;
import tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;
import tech.metacontext.ocnhfa.composer.cf.utils.io.MusicReader;
import tech.metacontext.ocnhfa.composer.cf.utils.io.MusicWriter;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.CFScore;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Clef;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ECStudio extends Studio {

  private List<ECComposer> composers;
  private int generation;
  private double threshold;
  private double crossover_rate, mutation_rate;
  private Evaluator<MusicThread> eval_function;
  private Recombinator<MusicThread> crossover;
  private Mutator<MusicThread> mutate;
  private File project_dir = PROJECT_DIR;
  private File base_dir;
  private List<Pair<Double>[]> chart_data;
  private Map<String, List<Double[]>> score_data;

  public ECStudio(
          Evaluator<MusicThread> eval_function,
          Recombinator<MusicThread> crossover,
          Mutator<MusicThread> mutate,
          double... parameters) {

    this(ComposerType.COMPOSE_STATIC, null, DEFAULT_GENERATION,
            eval_function, crossover, mutate, parameters);
  }

  public ECStudio(ComposerType type, Double threshold, int generation,
          Evaluator<MusicThread> eval_function,
          Recombinator<MusicThread> crossover,
          Mutator<MusicThread> mutate,
          double... parameters) {

    super(type);
    init(eval_function, crossover, mutate, parameters);
    if (threshold == null) {
      this.generation = generation;
      System.out.println("Generation = " + this.generation);
      System.out.println("Crossover Rate = " + this.crossover_rate);
      System.out.println("Mutation Rate = " + this.mutation_rate);
    } else {
      this.threshold = threshold;
      this.generation = generation;
      System.out.println("Threshold = " + this.threshold);
      var total = this.crossover_rate + this.mutation_rate;
      System.out.printf("Crossover : Mutation Rate = %.2f : %.2f\n",
              this.crossover_rate / total, this.mutation_rate / total);
    }
  }

  private void init(Evaluator<MusicThread> eval_function,
          Recombinator<MusicThread> crossover,
          Mutator<MusicThread> mutate,
          double... parameters) {

    this.eval_function = eval_function;
    this.crossover = crossover;
    this.mutate = mutate;
    this.crossover_rate = parameters.length == 2
            ? parameters[0] : Parameters.DEFAULT_CROSSOVER_RATE;
    this.mutation_rate = parameters.length == 2
            ? parameters[1] : Parameters.DEFAULT_MUTATION_RATE;
    this.chart_data = new ArrayList<>();
    this.score_data = new HashMap<>();
  }

  @Override
  public void run() {

    var date_time = LocalDateTime.now();

    assert this.getComposer_type() == ComposerType.COMPOSE
            || this.getComposer_type() == ComposerType.COMPOSE_STATIC :
            "Only COMPOSE and COMPOSE_STATIC Types allowed";

    this.setProject_name("EC" + "_" + this.getComposer_type() + "_" + createTimeBasedId());

    // Initialize composers
    preset_composers();
    this.composers.stream()
            .forEach(c -> {
              c.setFraction_mode(this.getFraction_mode());
              if (this.getComposer_type() == ComposerType.COMPOSE_STATIC) {
                c.x_pheromone_deposit_amount = 0.0;
                c.x_pheromone_evaporate_rate = 0.0;
                c.y_pheromone_deposit_amount = 0.0;
                c.y_pheromone_evaporate_rate = 0.0;
              }
            });

    int generation_count = 0;
    while (generation_count < this.generation
            && (threshold == 0.0 || this.composers.stream().anyMatch(not(ECComposer::isCompleted)))) {
      if (generation_count == 0) {
        System.out.println("\n***\n*** Initializing music threads\n***");
      }
      // removeLowest if g > 0
      // completeAll and eval_sort
      this.composers.stream()
              .filter(not(ECComposer::isCompleted))
              .forEach(c -> {
                System.out.printf("\nProcessing %s ", c.getId());
                c.init_population();
                c.completeAll();
              });
      System.out.println();
      chart_data.add(dumpAll(true));

      this.composers.stream()
              .filter(not(ECComposer::isCompleted))
              .map(c -> String.format("Evalutations applied for %s: %d", c.getId(), c.getEvalCount()))
              .forEach(System.out::println);

      System.out.println();
      System.out.printf("\n%s\nGeneration %d\n%s\n", LINE, ++generation_count, LINE);

      // Mutation / Crossover
      this.composers.stream()
              .filter(not(ECComposer::isCompleted))
              .peek(ECComposer::removeLowest)
              .forEach(this::ec_operation);
      System.out.println();

      this.composers.stream()
              .filter(not(ECComposer::isCompleted))
              .map(c -> String.format("Population size of %s: %d", c.getId(), c.getECMusicThreads().size()))
              .forEach(System.out::println);
    }

    chart_data.add(dumpAll(true));

    this.setQualified_threads(this.composers.stream()
            .collect(Collectors.toMap(
                    c -> c,
                    c -> c.getECMusicThreads().stream()
                            .limit(getTarget_size())
                            .map(Entry::getKey)
                            .collect(Collectors.toList())
            )));

    var time_elapsed = Duration.between(date_time, LocalDateTime.now());
    var sec = time_elapsed.getSeconds() % 60;
    var min = time_elapsed.toMinutes();
    System.out.printf("Computing time: %dm %ds\n", min, sec);
    this.composers.stream()
            .map(c -> String.format("Evalutations applied for %s: %d", c.getId(), c.getEvalCount()))
            .forEach(System.out::println);
  }

  public void ec_operation(ECComposer c) {

    System.out.printf("\nMutate/Crossover %s ", c.getId());
    var thread_num = this.getThread_number() / SELECT_DIV;
    IntStream.range(0, thread_num).forEach(i -> {
      if (this.threshold > 0.0) {
        var c_rate = this.crossover_rate / (this.crossover_rate + this.mutation_rate);
        if (getRandom().nextDouble() < c_rate) {
          System.out.print(c.crossover(i) ? "+" : "");
        } else {
          System.out.print(c.mutation(i) ? "." : "");
        }
      } else {
        switch (ECOperation.get()) {
          case crossover ->
            System.out.print(getRandom().nextDouble() < crossover_rate && c.crossover(i) ? "+" : "");
          case mutation ->
            System.out.print(getRandom().nextDouble() < mutation_rate && c.mutation(i) ? "." : "");
        }
      }
    });
  }

  @Override
  public void saveScore(Clef clef) {

    if (this.base_dir == null) {
      this.base_dir = new File(this.project_dir, "GENERATE_CF_" + createTimeBasedId());
    }

    if (Objects.isNull(this.getQualifiedThreads())) {
      throw new NoQualifiedThreadsSelectedException();
    }
    System.out.println("Saving Composer and Cantus Firmi ... ");
    this.getQualifiedThreads().entrySet().stream().forEach(e -> {
      var id = e.getKey().getId();

      var composer_path = MusicWriter.saveComposer(base_dir.getPath(), e.getKey());
      System.out.println(composer_path);

      var cf_path = new File(composer_path, "cantus_firmus");
      cf_path.mkdirs();
      IntStream.range(0, this.getTarget_size())
              .forEach(i -> {
                var filename = new File(cf_path, "cantus_firmus_" + (i + 1) + ".xml");
                var thread = e.getValue().get(i);
                MusicWriter.saveRoute(filename, thread);
              });

      var score_filename = composers.indexOf(e.getKey()) + "_score.musicxml";
      System.out.println("Saving Score ... " + score_filename);
      var score = new CFScore(id, "EC-" + this.getPreset());
      IntStream.rangeClosed(1, this.getTarget_size())
              .forEach(i -> {
                var thread = e.getValue().get(i - 1);
                var selected_clef = (clef == null)
                        ? Clef.selector(thread.getCf().getMiddle())
                        : clef;
                score.addMeasure(selected_clef, i, thread.getCf().getMelody());
              });
      score.writeMusicXML(new File(base_dir, score_filename));
    });
  }

  private void preset_composers() {

    System.out.println("Creating composers from preset ...");
    var path = new File(this.project_dir, this.getPreset());
    System.out.println("Searching Composers from " + path);
    Arrays.stream(path.listFiles(File::isDirectory)).forEach(System.out::println);
    var projects = Arrays.stream(path.listFiles(File::isDirectory))
            .filter(file -> this.getFolder() == null || this.getFolder().equals(file.getName()))
            .sorted()
            .map(MusicReader::getComposerXML)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    System.out.println(projects.size() + " folder(s) located.");
    this.setComposer_number(projects.size());
    createComposers();
    var it = this.composers.iterator();
    projects.forEach(p -> it.next().init(p, this.getThread_number()));
  }

  private void createComposers() {

    this.composers = IntStream.range(0, this.getComposer_number())
            .mapToObj(i -> this.getProject_name() + "_" + i)
            .map(id -> ECComposer.getInstance(eval_function, crossover, mutate).setLogger(id))
            .collect(Collectors.toList());
  }

  private Pair<Double>[] dumpAll(boolean listing) {

    Pair<Double>[] result = new Pair[composers.size()];

    for (int i = 0; i < this.composers.size(); i++) {
      var c = this.composers.get(i);
      if (c.isCompleted()) {
        continue;
      }
      System.out.println("\nComposer id = " + c.getId());

      c.eval_sort();
      var score = c.getECMusicThreads().stream()
              .map(Entry::getValue)
              .toArray(Double[]::new);
      if (this.score_data.get(c.getId()) == null) {
        this.score_data.put(c.getId(), new ArrayList<>());
      }
      this.score_data.get(c.getId()).add(score);

      var total_average = Arrays.stream(score)
              .limit(this.getThread_number())
              .mapToDouble(v -> v)
              .average().getAsDouble();

      var target = Math.min(this.getTarget_size(), this.getThread_number());
      var target_minimal = c.getECMusicThreads().get(target - 1).getValue();
      if (threshold > 0.0 && target_minimal >= threshold) {
        c.setCompleted(true);
      }

      if (listing) {
        IntStream.iterate(0, Math::incrementExact)
                .limit(target)
                .forEach(j -> {
                  assert c.getECMusicThreads().get(j).getValue().equals(score[j]);
                  System.out.printf("%d. %s score=%.2f\n", (j + 1),
                          c.getECMusicThreads().get(j).getKey().getCf(),
                          score[j]);
                });
      }

      System.out.println("Target Minimal = " + target_minimal);
      System.out.println("Population Average = " + total_average);

      result[i] = new Pair(total_average, target_minimal);
    }
    return result;
  }

  public File getProject_dir() {

    return project_dir;
  }

  public void setProject_dir(File project_dir) {

    this.project_dir = project_dir;
  }

  public File getBase_dir() {

    return base_dir;
  }

  public void setBase_dir(File base_dir) {

    this.base_dir = base_dir;
  }

  public List<ECComposer> getComposers() {

    return this.composers;
  }

  public int getGeneration() {

    return generation;
  }

  public List<Pair<Double>[]> getChart_data() {

    return chart_data;
  }

  public Map<String, List<Double[]>> getScore_data() {

    return score_data;
  }
}
