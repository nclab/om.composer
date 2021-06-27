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
package tech.metacontext.ocnhfa.composer.cf.model;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import tech.metacontext.ocnhfa.composer.cf.ex.NoQualifiedThreadsSelectedException;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.LINE;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.PROJECT_DIR;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.createTimeBasedId;
import tech.metacontext.ocnhfa.composer.cf.model.constraints.MusicThreadConstraint;
import tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType;
import static tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType.*;
import tech.metacontext.ocnhfa.composer.cf.model.enums.EcclesiasticalMode;
import tech.metacontext.ocnhfa.composer.cf.utils.io.MusicReader;
import tech.metacontext.ocnhfa.composer.cf.utils.io.MusicWriter;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Clef;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Score;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Studio {

  private ComposerType composer_type;
  private int composer_number;
  private int thread_number;
  private int target_size;
  private EcclesiasticalMode ecclesiastical_mode;
  private FractionMode fraction_mode;

  //Parameters for COMPOSE | COMPOSE_STATIC
  private String project_name;
  private String preset;
  private String folder;

  private List<Composer> composers;
  private Map<Composer, List<MusicThread>> qualified_threads;

  public Studio(ComposerType composer_type) {

    this.composer_type = composer_type;
    this.target_size = Parameters.DEFAULT_TARGET_SIZE;
  }

  public Studio() {

    this(DEVELOP_MODAL_STANDARD);
  }

  public void run() {

    this.project_name = this.composer_type + "_" + createTimeBasedId();

    switch (this.composer_type) {
      case COMPOSE_STATIC, COMPOSE ->
        preset_composers();
      case DEVELOP_STANDARD ->
        standard_composers();
      case DEVELOP_MODAL_STANDARD ->
        modal_composers();
    }
    // Generate composers from specified source and compose.
    this.composers.stream()
            .peek(c -> System.out.printf("\nProcessing %s ", c.getId()))
            .forEach(c -> {
              c.setFraction_mode(this.fraction_mode);
              if (this.composer_type == COMPOSE_STATIC) {
                c.x_pheromone_deposit_amount = 0.0;
                c.x_pheromone_evaporate_rate = 0.0;
                c.y_pheromone_deposit_amount = 0.0;
                c.y_pheromone_evaporate_rate = 0.0;
              }
              while (!c.isAimAchieved()) {
                c.navigate();
              }
            });
    System.out.println();
    // Select qualified threads by composer
    this.qualified_threads = IntStream.range(0, this.composers.size())
            .mapToObj(i -> Map.entry(i, this.composers.get(i)))
            .collect(Collectors.toMap(Entry::getValue,
                    entry -> entry.getValue().getAnts().stream()
                            .filter(new MusicThreadConstraint())
                            .distinct()
                            .collect(Collectors.toList())));

    // Calculate qualified rate etc.
    var average = qualified_threads.entrySet().stream()
            .peek(e -> {
              System.out.printf("%s\nComposer: %s\n", LINE, e.getKey().getId());
              if (composer_type == COMPOSE || composer_type == COMPOSE_STATIC) {
                System.out.println("based on " + e.getKey().getPreset_source());
              }
            })
            .peek(e -> System.out.printf("Qualified melodies = %d/%d\n", e.getValue().size(), e.getKey().getThread_number()))
            .mapToDouble(entry -> 100.0 * entry.getValue().size() / entry.getKey().getThread_number())
            .peek(rate -> System.out.printf("Qualified rate = %2.2f%%\n", rate))
            .average().getAsDouble();
    System.out.printf("%s\nAverage qualified rate from %d composers = %2.2f%%\n",
            LINE, composers.size(), average);
  }

  public void saveScore(Clef clef) {

    if (Objects.isNull(this.qualified_threads)) {
      throw new NoQualifiedThreadsSelectedException();
    }
    var project_dir = new File(PROJECT_DIR, this.project_name);
    this.qualified_threads.entrySet().stream().forEach(e -> {

      var id = e.getKey().getId();
      System.out.println("Saving Composer and Cantus Firmi ...");
      var composer_path = MusicWriter.saveComposer(project_dir.getPath(), e.getKey());

      var cf_path = new File(composer_path, "cantus_firmus");
      cf_path.mkdirs();
      var selected_pool = e.getValue().stream()
              .sorted()
              .limit(target_size)
              .collect(Collectors.toList());
      IntStream.range(0, selected_pool.size())
              .forEach(i -> {
                var filename = new File(cf_path, "cantus_firmus_" + i + ".xml");
                var thread = selected_pool.get(i);
                MusicWriter.saveRoute(filename, thread);
              });

      System.out.println("Saving Score ...");
      var score = new Score(id, this.preset);
      IntStream.rangeClosed(1, selected_pool.size())
              .forEach(i -> {
                var thread = selected_pool.get(i - 1);
                var selected_clef = (clef == null)
                        ? Clef.selector(thread.getCf().getMiddle())
                        : clef;
                score.addMeasure(selected_clef, i,
                        thread.getCf().getMelody());
              });
      score.saveScore(new File(project_dir, composers.indexOf(e.getKey()) + "_score.musicxml"));
    });
  }

  private void preset_composers() {

    System.out.println("Creating composers from preset ...");
    var path = new File(PROJECT_DIR, this.preset);
    System.out.println("Searching Composers from " + path.getPath());
    var projects = Stream.of(path.listFiles(File::isDirectory))
            .filter(file -> this.folder == null || this.folder.equals(file.getName()))
            .sorted()
            .map(MusicReader::getComposerXML)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    System.out.println(projects.size() + " folder(s) located.");
    createComposers(projects.size());
    var it = this.composers.iterator();
    projects.forEach(p -> it.next().init(p, this.thread_number));
  }

  private void modal_composers() {

    System.out.println("Creating modal composers ...");
    createComposers(EcclesiasticalMode.values().length - 1);
    var it = this.composers.iterator();
    Stream.iterate(0, i -> it.hasNext(), Math::incrementExact)
            .map(i -> EcclesiasticalMode.values()[i])
            .forEach(mode -> it.next().init(this.thread_number, mode));
  }

  private void standard_composers() {

    System.out.println("Creating standard composers ...");
    createComposers(this.composer_number);
    this.composers.forEach(c -> c.init(this.thread_number, this.ecclesiastical_mode));
  }

  private void createComposers(int number) {

    this.composers = IntStream.range(0, number)
            .mapToObj(i -> Composer.getInstance().setLogger(project_name + "_" + i))
            /*
                 * DO NOT use member reference like:
                 * .mapToObj(i -> project_name + "_" + i)
                 * .map(Composer.getInstance()::setLogger)
             */
            .collect(Collectors.toList());
  }

  public String getProject_name() {

    return this.project_name;
  }

  public void setProject_name(String project_name) {

    this.project_name = project_name;
  }

  public String getPreset() {

    return preset;
  }

  public String getFolder() {

    return folder;
  }

  public Studio setModel(String preset, String folder) {

    this.preset = preset;
    this.folder = folder;
    return this;
  }

  public ComposerType getComposer_type() {

    return composer_type;
  }

  public void setComposer_type(ComposerType composer_type) {

    this.composer_type = composer_type;
  }

  public int getComposer_number() {

    return composer_number;
  }

  public Studio setComposer_number(int composer_number) {

    this.composer_number = composer_number;
    return this;
  }

  public int getThread_number() {

    return thread_number;
  }

  public Studio setThread_number(int thread_number) {

    this.thread_number = thread_number;
    return this;
  }

  public EcclesiasticalMode getEcclesiastical_mode() {

    return this.ecclesiastical_mode;
  }

  public Studio setEcclesiastical_Mode(EcclesiasticalMode ecclesiastical_mode) {

    this.ecclesiastical_mode = ecclesiastical_mode;
    return this;
  }

  public int getTarget_size() {

    return target_size;
  }

  public Studio setTarget_size(int target_size) {

    this.target_size = target_size;
    return this;
  }

  public FractionMode getFraction_mode() {

    return fraction_mode;
  }

  public Studio setFraction_mode(FractionMode fraction_mode) {

    this.fraction_mode = fraction_mode;
    return this;
  }

  public Map<Composer, List<MusicThread>> getQualifiedThreads() {

    return this.qualified_threads;
  }

  public void setQualified_threads(Map<Composer, List<MusicThread>> qualified_threads) {

    this.qualified_threads = qualified_threads;
  }
}
