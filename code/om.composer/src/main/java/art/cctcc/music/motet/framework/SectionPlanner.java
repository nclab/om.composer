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
package art.cctcc.music.motet.framework;

import art.cctcc.music.motet.graphs.SectionNode;
import art.cctcc.music.motet.graphs.SectionPath;
import static art.cctcc.music.motet.model.enums.SectionType.CF;
import java.util.ArrayList;
import java.util.List;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import tech.metacontext.ocnhfa.antsomg.model.Ant;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SectionPlanner implements Ant<SectionTrace>, Comparable {

  SectionTrace currentTrace;
  List<SectionTrace> route;
  private boolean completed;

  private List<SectionNode> sections;

  public SectionPlanner(SectionNode node) {

    var move = new StandardMove<SectionPath>(new SectionPath(null, node, 0));
    this.currentTrace = new SectionTrace(move);
    this.route = new ArrayList<>();
    this.sections = new ArrayList<>();
  }

  public SectionPlanner() {

    this(CF.node);
  }

  @Override
  public List<SectionTrace> getRoute() {

    return this.route;
  }

  @Override
  public void setCurrentTrace(SectionTrace trace) {

    this.addCurrentTraceToRoute();
    this.currentTrace = trace;
  }

  @Override
  public SectionTrace getCurrentTrace() {

    return this.currentTrace;
  }

  @Override
  public void addCurrentTraceToRoute() {

    this.route.add(currentTrace);
    this.sections.add(currentTrace.getMove().getSelected().getTo());
  }

  @Override
  public int compareTo(Object o) {

    if (o instanceof SectionPlanner a) {
      return this.sections.size() - a.sections.size();
    }
    return 0;
  }

  public boolean isCompleted() {

    return completed;
  }

  public void setCompleted(boolean completed) {

    this.completed = completed;
  }

  public List<SectionNode> getSections() {

    return sections;
  }

  @Override
  public String toString() {

    return "SectionPlan{" + sections + '}';
  }

}
