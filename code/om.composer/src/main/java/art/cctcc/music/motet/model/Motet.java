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
package art.cctcc.music.motet.model;

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.framework.CptComposer;
import art.cctcc.music.cpt.framework.CptEvolution;
import art.cctcc.music.cpt.graphs.x.CptMusicSpace;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.cpt.model.CptCounterpoint;
import art.cctcc.music.cpt.model.CptMelody;
import static art.cctcc.music.cpt.model.enums.CptTask.*;
import art.cctcc.music.motet.graphs.SectionNode;
import art.cctcc.music.motet.model.enums.SectionType;
import static art.cctcc.music.motet.model.enums.SectionType.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.LINE;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Motet {

  private final int number;
  private final List<SectionNode> scheme;
  private Map<String, CptMusicSpace> graph_x;
  private List<CptCantusFirmus> cf_list;
  private List<Map.Entry<SectionType, CptMelody>> composition;
  private List data;
  private boolean test;
  private boolean parallel;
  private boolean chromatic;

  public Motet(int number, List<SectionNode> scheme) {

    this.number = number;
    this.scheme = scheme;
    this.composition = new ArrayList<>();
    this.data = new ArrayList();
  }

  public void setGraph_x(Map<String, CptMusicSpace> graph_x) {

    this.graph_x = graph_x;
  }

  public void select_cf(List<CptCantusFirmus> cf_list) {

    var cf_num = scheme.stream()
            .map(node -> node.type)
            .filter(CF::equals).count();
    var section = cf_list.size() / (int) cf_num;
    var cf_selected = IntStream.range(0, (int) cf_num)
            .map(i -> i * section + (section == 0 ? 0 : getRandom().nextInt(section)))
            .mapToObj(cf_list::get)
            .collect(Collectors.toList());
    this.cf_list = cf_selected;
  }

  public void setTest(boolean test) {

    this.test = test;
  }

  public void setParallel(boolean parallel) {

    this.parallel = parallel;
  }

  public void setChromatic(boolean chromatic) {

    this.chromatic = chromatic;
  }

  public void compose() {

    System.out.println(this);
    var cf_deque = new ArrayDeque<>(cf_list);
    var selected_cf = new AtomicReference<CptCantusFirmus>();
    var treble = new AtomicReference<CptComposer>();
    var bass = new AtomicReference<CptComposer>();

    IntStream.range(0, scheme.size()).forEach(i -> {
      System.out.print(scheme.get(i).type + ":");
      switch (scheme.get(i).type) {
        case CF -> {
          selected_cf.set(cf_deque.pop());
          composition.add(Map.entry(CF, selected_cf.get()));
          var id_cf = selected_cf.get().getId();
          treble.set(CptComposer.getInstance(id_cf + "-treble", selected_cf.get(), true));
          bass.set(CptComposer.getInstance(id_cf + "-bass", selected_cf.get(), false));
          Stream.of(treble, bass)
                  .map(AtomicReference::get)
                  .peek(cpt_composer -> cpt_composer.setParallel(this.parallel))
                  .peek(cpt_composer -> cpt_composer.setChromatic(this.chromatic))
                  .peek(CptComposer::init_graphs)
                  .peek(cpt_composer -> {
                    if (graph_x.containsKey(cpt_composer.getId())) {
                      cpt_composer.setX(graph_x.get(cpt_composer.getId()));
                      cpt_composer.setTask(DEVELOP_SECONDARY);
                    } else if (graph_x.containsKey("PRESET")) {
                      cpt_composer.setX(graph_x.get("PRESET").duplicate());
                    }
                    if (test) {
                      cpt_composer.setPopulation(CPT_TESTING_POPULATION);
                    }
                  })
                  .peek(CptComposer::init_population)
                  .peek(CptComposer::navigate)
                  .forEach(c -> graph_x.putIfAbsent(c.getId(), c.getX()));
          data.add(new CptComposer[]{treble.get(), bass.get()});
        }
        case CPT_TREBLE ->
          composition.add(Map.entry(CPT_TREBLE, generateCpt(treble.get())));
        case CPT_BASS ->
          composition.add(Map.entry(CPT_BASS, generateCpt(bass.get())));
      }
      System.out.println();
    });
  }

  private CptCounterpoint generateCpt(CptComposer cpt_composer) {

    cpt_composer.setTask(COMPOSE);
    if (test) {
      cpt_composer.setPopulation(CPT_TESTING_POPULATION);
    }
    cpt_composer.init_population();
    cpt_composer.navigate();
    var ec = new CptEvolution(cpt_composer,
            test ? MOTET_COMPOSE_THRESHOLD_TEST : MOTET_COMPOSE_THRESHOLD,
            MOTET_COMPOSE_GENERATION);
    data.add(ec);
    ec.run();
    return cpt_composer.getAnts().get(getRandom().nextInt(MOTET_COMPOSE_TARGET_SIZE)).getCpt();
  }

  public String getId() {

    return "motet-" + number;
  }

  public String getTitle() {

    return "Motet No." + number;
  }

  public List<CptCantusFirmus> getCf_list() {

    return cf_list;
  }

  public List<SectionNode> getScheme() {

    return scheme;
  }

  public String getSchemeString() {

    return this.scheme.stream()
            .map(SectionNode::toString)
            .collect(Collectors.joining(", "));
  }

  public List<Map.Entry<SectionType, CptMelody>> getComposition() {

    return composition;
  }

  public List getData() {

    return data;
  }

  @Override
  public String toString() {

    var output = new StringBuilder();
    if (composition.size() < scheme.size()) {
      output.append(this.number).append(":").append(scheme.toString());
    } else {
      composition.forEach(e -> {
        output.append(LINE)
                .append(System.lineSeparator())
                .append(this.number).append(":")
                .append(System.lineSeparator())
                .append(LINE)
                .append(System.lineSeparator());
        switch (e.getKey()) {
          case CF -> {
            output.append(CF.toString()).append(":")
                    .append(e.getValue())
                    .append(System.lineSeparator());
          }
          case CPT_TREBLE -> {
            output.append(CPT_TREBLE.toString()).append(":")
                    .append(e.getValue())
                    .append(System.lineSeparator())
                    .append(CF.toString()).append(":")
                    .append(((CptCounterpoint) e.getValue()).getCf())
                    .append(System.lineSeparator());
          }
          case CPT_BASS -> {
            output.append(CF.toString()).append(":")
                    .append(((CptCounterpoint) e.getValue()).getCf())
                    .append(System.lineSeparator())
                    .append(CPT_BASS.toString()).append(":")
                    .append(e.getValue())
                    .append(System.lineSeparator());
          }
        }
      });
      output.append(LINE);
    }
    return output.toString();
  }
}
