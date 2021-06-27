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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.function.Predicate.not;
import java.util.logging.Level;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Evaluator;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Mutator;
import tech.metacontext.ocnhfa.composer.cf.ec.function.Recombinator;
import tech.metacontext.ocnhfa.composer.cf.model.Composer;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.CF_LENGTH_LOWER;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.SELECT_DIV;
import tech.metacontext.ocnhfa.composer.cf.model.constraints.MusicThreadConstraint;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class ECComposer extends Composer {

  private Evaluator<MusicThread> eval_function;
  private Recombinator<MusicThread> recombinator;
  private Mutator<MusicThread> mutator;
  private List<Entry<MusicThread, Double>> music_threads;

  private int eval_count;
  private boolean completed;

  public static synchronized ECComposer getInstance(
          Evaluator<MusicThread> eval_function,
          Recombinator<MusicThread> recombinator,
          Mutator<MusicThread> mutator) {

    return new ECComposer(eval_function, recombinator, mutator);
  }

  private ECComposer(Evaluator<MusicThread> eval_function,
          Recombinator<MusicThread> recombinator,
          Mutator<MusicThread> mutator) {

    this.music_threads = new ArrayList<>();
    this.eval_function = eval_function;
    this.recombinator = recombinator;
    this.mutator = mutator;
  }

  @Override
  public ECComposer setLogger(String id) {

    super.setLogger(id);
    return this;
  }

  @Override
  public void init_population() {

    while (this.music_threads.size() < this.getThread_number()) {
      addThread();
    }
    this.setNavigation_count(0);
  }

  @Override
  public void init() {

    System.out.println("Initializing ECComposer, id = " + this.id);

    this.logger.log(Level.INFO, "Initializing Composer, id = {0}", this.id);

    this.logger.log(Level.INFO, "Music thread number = {0}, ecclesiastical mode = {1}",
            new Object[]{this.getThread_number(), this.getEcclesiastical_mode().name()});

    this.logger.log(Level.INFO, "Initializing MusicSpace...");
    init_graphs();

    this.logger.log(Level.INFO, "Initializing music threads...");
    init_population();
  }

  public void addThread() {

    this.addThread(new MusicThread(this.getEcclesiastical_mode(),
            null, this.getX().getStart(), this.logger));
  }

  public void addThread(MusicThread mt) {

    this.music_threads.add(new SimpleEntry(mt, -1.0));
  }

  public void completeAll() {

    while (!isAimAchieved()) {
      navigate();
    }
    System.out.println();
    this.eval_sort();
  }

  @Override
  public void navigate() {

    var count = this.getNavigation_count() + 1;
    this.setNavigation_count(count);
    this.logger.log(Level.INFO, "*** navigating, navigation_count = {0}", count);
    this.setToCadence(count > CF_LENGTH_LOWER);

    this.music_threads.stream()
            .map(Entry::getKey)
            .filter(not(MusicThread::isCompleted))
            .forEach(thread -> {
              do {
                if (nav_y(thread, nav_x(thread))) break;
              } while (true);
              this.logger.log(Level.INFO, thread.toString());
            });
    evaporate();
    System.out.print(".");
  }

  @Override
  public boolean isAimAchieved() {

    return music_threads.stream().map(Entry::getKey).allMatch(MusicThread::isCompleted);
  }

  public void removeLowest() {

    this.music_threads = this.music_threads
            .subList(0, this.getThread_number() - this.getThread_number() / SELECT_DIV);
  }

  public MusicThread getNewThread() {

    var nav_count = 0;
    this.setToCadence(false);
    var thread = new MusicThread(this.getEcclesiastical_mode(), null,
            this.getX().getStart(), this.logger);
    do {
      if (nav_y(thread, nav_x(thread))) {
        nav_count++;
        this.setToCadence(nav_count > CF_LENGTH_LOWER);
      }
    } while (!thread.isCompleted());
    return thread;
  }

  /**
   * Crossover with a newly generated parent 2.
   *
   * @param i index of selected parent 1.
   * @return if successfully produced a child put into population.
   */
  public boolean crossover(int i) {

    var p1 = this.music_threads.get(i).getKey();
    var p2 = this.getNewThread();
    var c = this.recombinator.recombine(new Pair(p1, p2));

    if (Objects.isNull(c)
            || this.music_threads.stream()
                    .anyMatch(e -> e.getKey().equals(c))) {
      return false;
    }
    c.setCompleted(true);
    this.addThread(c);
    return true;
  }

  /**
   * Mutate.
   *
   * @param i index of selected individual to be mutated.
   * @return if successfully produced a mutant put into population.
   */
  public boolean mutation(int i) {

    var p0 = this.music_threads.get(i).getKey();
    var pm = this.mutator.mutate(p0);

    if (Objects.isNull(pm)
            || this.music_threads.stream()
                    .anyMatch(e -> e.getKey().equals(pm))) {
      return false;
    }
    pm.setCompleted(true);
    this.addThread(pm);
    return true;
  }

  public void eval_sort() {

    this.music_threads.stream()
            .filter(e -> e.getValue() < 0)
            .forEach(this::evalEntry);
    this.music_threads.sort(Entry.comparingByValue(Comparator.reverseOrder()));
  }

  public Evaluator<MusicThread> getEval_function() {

    return eval_function;
  }

  public void setEval_function(Evaluator<MusicThread> eval_function) {

    this.eval_function = eval_function;
  }

  public Recombinator<MusicThread> getCrossover() {

    return recombinator;
  }

  public void setCrossover(Recombinator<MusicThread> crossover) {

    this.recombinator = crossover;
  }

  public Mutator<MusicThread> getMutate() {

    return mutator;
  }

  public void setMutate(Mutator<MusicThread> mutate) {

    this.mutator = mutate;
  }

  public List<Entry<MusicThread, Double>> getECMusicThreads() {

    return music_threads;
  }

  public void setECMusicThreads(List<Entry<MusicThread, Double>> music_threads) {

    this.music_threads = music_threads;
  }

  @Override
  public List<MusicThread> getMusicThreads() {

    return this.music_threads.stream()
            .map(Entry::getKey)
            .collect(Collectors.toList());
  }

  public boolean isCompleted() {

    return completed;
  }

  public void setCompleted(boolean completed) {

    this.completed = completed;
  }

  public int getEvalCount() {

    return eval_count;
  }

  public void evalEntry(Entry<MusicThread, Double> e) {

    var thread = e.getKey();
    e.setValue(new MusicThreadConstraint().test(thread)
            ? this.eval_function.apply(thread) : 0.0);
    ++eval_count;
  }
}
