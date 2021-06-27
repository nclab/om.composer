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
package tech.metacontext.ocnhfa.antsomg.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.*;
import tech.metacontext.ocnhfa.antsomg.model.Graph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <E>
 * @param <V>
 */
public abstract class StandardGraph<E extends StandardEdge<V>, V extends StandardVertex>
        implements Graph<E, V, StandardMove<E>> {

  private List<E> edges;
  private V start;
  protected double alpha, beta;
  private FractionMode fraction_mode;

  public enum FractionMode {

    Power, Coefficient, Power_Multiply;
  }

  public StandardGraph(double alpha, double beta) {

    this.edges = new ArrayList<>();
    this.alpha = alpha;
    this.beta = beta;
    this.fraction_mode = FractionMode.Coefficient;
  }

  public StandardGraph() {

    this(ALPHA, BETA);
  }

  public double getFraction(E edge) {

    return switch (this.fraction_mode) {
      case Power ->
        Math.pow(edge.getPheromoneTrail(), this.alpha)
        + Math.pow(1.0 / edge.getCost(), this.beta);
      case Power_Multiply ->
        Math.pow(edge.getPheromoneTrail(), this.alpha)
        * Math.pow(1.0 / edge.getCost(), this.beta);
      case Coefficient ->
        edge.getPheromoneTrail() * this.alpha
        + 1.0 / edge.getCost() * this.beta;
    };
  }

  @Override
  public StandardMove<E> move(V current, double pheromone_deposit,
          double explore_chance, double... parameters) {

    if (this.fraction_mode == FractionMode.Power_Multiply) {
      return move_power_multiply(current, pheromone_deposit, explore_chance);
    }

    return move(this.queryByVertex(current), pheromone_deposit, explore_chance);
  }

  public StandardMove<E> move(List<E> paths, double pheromone_deposit, double explore_chance) {

    var fractions = new ArrayList<Double>();
    var sum = paths.stream()
            .mapToDouble(this::getFraction)
            .peek(fractions::add)
            .sum();
    var r = new AtomicReference<Double>(StandardParameters.getRandom().nextDouble() * sum);
    var isExploring = StandardParameters.getRandom().nextDouble() < explore_chance;
    var selected = isExploring
            ? paths.get(StandardParameters.getRandom().nextInt(paths.size()))
            : IntStream.range(0, paths.size())
                    .filter(i -> r.getAndSet(r.get() - fractions.get(i)) < fractions.get(i))
                    .mapToObj(paths::get)
                    .findFirst().get();
    selected.addPheromoneDeposit(pheromone_deposit);
    return StandardMove.getInstance(isExploring, paths, selected);
  }

  /**
   * Method move() for power_multiply mode.
   *
   * @param current
   * @param pheromone_deposit
   * @param explore_chance
   * @return StandardMove
   */
  public StandardMove<E> move_power_multiply(V current, double pheromone_deposit,
          double explore_chance) {

    assert this.fraction_mode == FractionMode.Power_Multiply;
    return move_power_multiply(this.queryByVertex(current), pheromone_deposit, explore_chance);
  }

  public StandardMove<E> move_power_multiply(List<E> paths, double pheromone_deposit,
          double explore_chance) {

    var isExploring = StandardParameters.getRandom().nextDouble() < explore_chance;
    var selected = switch (isExploring ? 1 : 0) {
      case 1 ->
        paths.get(StandardParameters.getRandom().nextInt(paths.size()));
      default -> {
        var fractions = new ArrayList<Double>();
        var sum = paths.stream()
                .mapToDouble(this::getFraction)
                .peek(fractions::add)
                .sum();
        var r = new AtomicReference<Double>(StandardParameters.getRandom().nextDouble() * sum);
        yield IntStream.range(0, paths.size())
        .filter(i -> r.getAndSet(r.get() - fractions.get(i)) <= fractions.get(i))
        .mapToObj(paths::get)
        .findFirst().orElseThrow(() -> new NoSuchElementException("Fractions = " + fractions + ", r=" + r));
      }
    };
    selected.addPheromoneDeposit(pheromone_deposit);
    return StandardMove.getInstance(isExploring, paths, selected);
  }

  @Override
  public List<E> queryByVertex(V vertex) {

    return this.edges.stream()
            .filter(edge -> Objects.equals(edge.getFrom(), vertex))
            .collect(Collectors.toList());
  }

  @Override
  public String asXML() {

    var doc = DocumentHelper.createDocument();
    doc.setXMLEncoding("UTF-8");
    var root = doc.addElement(this.getClass().getSimpleName());
    getEdges().stream().forEach(e -> {
      var edge = root.addElement(e.getClass().getSimpleName());
      edge.addElement("from").setText(e.getFrom().getName());
      edge.addElement("to").setText(e.getTo().getName());
      edge.addElement("cost").setText(String.valueOf(e.getCost()));
      edge.addElement("pheromoneTrail").setText(String.valueOf(e.getPheromoneTrail()));
    });
    // return doc.asXML();
    var format = OutputFormat.createPrettyPrint();
    format.setExpandEmptyElements(true);
    format.setSuppressDeclaration(true);
    var stringWriter = new StringWriter();
    var writer = new XMLWriter(stringWriter, format);
    try {
      writer.write(doc);
    } catch (IOException ex) {
      Logger.getLogger(StandardGraph.class.getName()).log(Level.SEVERE, null, ex);
    }
    return stringWriter.toString();
  }

  public boolean isBlank() {

    return this.getEdges().stream()
            .map(E::getPheromoneTrail)
            .allMatch(ph -> ph == 0.0);
  }

  public String asGraphviz() {

    return String.format("digraph %s {\n%s\n}",
            this.getClass().getSimpleName(),
            getEdges().stream().map(
                    path -> isBlank()
                            ? String.format("\t%s -> %s [ label=<c=%.1f> ];",
                                    path.getFrom().getName(),
                                    path.getTo().getName(),
                                    path.getCost())
                            : String.format("\t%s -> %s [ label=<c=%.1f, pher=%.2f> ];",
                                    path.getFrom().getName(),
                                    path.getTo().getName(),
                                    path.getCost(),
                                    path.getPheromoneTrail())
            ).collect(Collectors.joining("\n"))
    );
  }

  public String asCypher() {

    String vertices = getEdges().stream()
            .flatMap(path -> Stream.of(path.getFrom(), path.getTo()))
            .filter(Objects::nonNull)
            .distinct()
            .map(v -> String.format("(%s:%s{name:\"%s\"})", v.getName(), v.getClass().getSimpleName(), v.getName()))
            .collect(Collectors.joining(", "));

    String cypher = String.format("CREATE %s;", vertices);

    cypher += String.format("MATCH %s CREATE %s;",
            vertices,
            getEdges().stream()
                    .map(path -> String.format("(%s)-[:TO]->(%s)",
                    path.getFrom().getName(), path.getTo().getName()))
                    .collect(Collectors.joining(", ")));

    return cypher;
  }

  public void addEdges(E... edges) {

    this.edges.addAll(List.of(edges));
  }

  public List<E> getEdges() {

    return this.edges;
  }

  public void setEdges(List<E> edges) {

    this.edges = edges;
  }

  public V getStart() {

    return start;
  }

  public void setStart(V start) {

    this.start = start;
  }

  public FractionMode getFraction_mode() {

    return fraction_mode;
  }

  public void setFraction_mode(FractionMode fraction_mode) {

    this.fraction_mode = fraction_mode;
  }

}
