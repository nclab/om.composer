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
package art.cctcc.music.cpt.graphs.y_cpt_cf;

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.cpt.ex.EmptyGraphException;
import art.cctcc.music.cpt.ex.ImmatureCptCfPitchSpaceException;
import art.cctcc.music.cpt.graphs.y_cpt.*;
import art.cctcc.music.cpt.model.CptCadence;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.cpt.model.enums.CptContrapuntalMotion;
import art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode;
import art.cctcc.music.cpt.model.enums.CptPitch;
import static art.cctcc.music.cpt.model.enums.CptPitch.*;
import static art.cctcc.music.cpt.model.enums.IntervalQuality.*;
import static art.cctcc.music.utils.CptCalculator.*;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.getRandom;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CptCfPitchSpace extends StandardGraph<CptPitchPath, CptPitchNode> {

  private CptCantusFirmus cf;
  private boolean treble;
  private List<Set<CptPitchPath>> loci;
  private List<CptCadence> cadences;

  private CptPitchSpace y_cpt = CptPitchSpace.getInstance();

  public CptCfPitchSpace(CptCantusFirmus cf, boolean treble) {

    this(ALPHA, BETA, cf, treble);
  }

  public CptCfPitchSpace(double alpha, double beta, CptCantusFirmus cf, boolean treble) {

    super(alpha, beta);
    this.cf = cf;
    this.treble = treble;
    this.cadences = this.cf.getMode().getCadences().stream()
            .filter(c -> !isVoiceOverlappingAtLocus(treble, c.getFormula().getLast(), cf.length() - 1))
            .collect(Collectors.toList());
  }

  public void setYCpt(CptPitchSpace y_cpt) {

    this.y_cpt = y_cpt;
  }

  public CptPitchSpace getYCpt() {

    return this.y_cpt;
  }

  @Override
  public final void init_graph() throws ImmatureCptCfPitchSpaceException {

    var starts = cf.getMode().getTerminals(this.treble).stream()
            .filter(pitch -> (!isVoiceOverlappingAtLocus(this.treble, pitch, 0)))
            .map(pitch -> CptPitchPath.of(null, pitch))
            .collect(Collectors.toList());
    this.setEdges(starts);

    IntStream.rangeClosed(1, cf.length() - 3).mapToObj(locus
            -> this.loci.get(locus - 1).stream()
                    .flatMap(path -> y_cpt.queryByVertex(path.getTo()).stream())
                    .filter(path -> !hasStylisticDepartures(path, locus))
                    .map(CptPitchPath::new)
                    .toArray(CptPitchPath[]::new))
            .forEach(this::addEdges);

    this.loci.get(cf.length() - 3).forEach(path -> {
      var node = path.getTo();
      this.cadences.stream()
              .map(ca -> new Pair<>(ca.getPathToCadence(node), ca.getPitchPath()))
              .filter(p -> p.e1() != null)
              .map(p -> Map.entry(p, CptPitchPath.of(path.getFrom(), p.e1().getFrom())))
              .filter(e -> e.getValue().melodicFeasible())
              .filter(e -> !hasStylisticDepartures(e.getValue(), cf.length() - 3))
              .filter(e -> !hasStylisticDepartures(e.getKey().e1(), cf.length() - 2))
              .forEach(e -> {
                this.addEdges(cf.length() - 2, e.getKey().e1());
                this.addEdges(cf.length() - 1, e.getKey().e2());
                if (cf.getMode().equals(CptEcclesiasticalMode.Aeolian)) {
                  path.setTo(e.getKey().e1().getFrom());
                }
              });
    });
    if (this.loci.size() == cf.length()) {
      IntStream.iterate(cf.length() - 2, i -> i >= 0, i -> i - 1)
              .forEach(locus -> {
                var dead_ends = this.loci.get(locus).stream()
                        .filter(path -> this.queryByVertex(locus + 1, path.getTo()).isEmpty())
                        .collect(Collectors.toList());
                this.loci.get(locus).removeAll(dead_ends);
              });
    } else {
      throw new ImmatureCptCfPitchSpaceException(cf, treble, this.asGraphviz());
    }
  }

  public BigDecimal cpt_count() {

    if (this.getLoci().size() < this.cf.length()) {
      return BigDecimal.ZERO;
    }
    var matrix = matrix_power(pitchSpaceToMatrix(this), this.getLoci().size() - 1);
    return matrix.toCount();
  }

  @Override
  public void addEdges(CptPitchPath... paths) {

    loci.add(new HashSet<>(List.of(paths)));
  }

  public void addEdges(int locus, CptPitchPath... paths) {

    if (locus < loci.size()) {
      loci.get(locus).addAll(Arrays.asList(paths));
    } else {
      this.addEdges();
      this.addEdges(locus, paths);
    }
  }

  @Override
  public List<CptPitchPath> getEdges() {

    return getLoci().stream().flatMap(Set::stream).collect(Collectors.toList());
  }

  @Override
  public void setEdges(List<CptPitchPath> edges) {

    this.loci = new ArrayList<>();
    this.loci.add(new HashSet<>(edges));
  }

  public CptPitchMove getMove(int locus, CptPitchNode current, double explore_chance) {

    var paths = this.queryByVertex(locus, current);
    var fractions = new ArrayList<Double>();
    var sum = paths.stream()
            .mapToDouble(this::getFraction)
            .peek(fractions::add)
            .sum();
    var r = new AtomicReference<Double>(getRandom().nextDouble() * sum);
    var isExploring = getRandom().nextDouble() < explore_chance;
    var selected = isExploring || paths.size() == 1
            ? paths.get(getRandom().nextInt(paths.size()))
            : IntStream.range(0, paths.size())
                    .filter(i -> r.getAndSet(r.get() - fractions.get(i)) < fractions.get(i))
                    .mapToObj(paths::get)
                    .findFirst().get();
    return new CptPitchMove(isExploring, paths, selected);
  }

  public CptPitchMove move(CptPitchMove move, double pheromone_deposit) {

    move.getSelected().addPheromoneDeposit(pheromone_deposit);
    return move;
  }

  /**
   * Find CptPitchPaths that come from the same pitch.
   *
   * @param locus locus, must greater than 0.
   * @param pitch specified origin CptPitchNode.
   * @return List of qualified CptPitchPath.
   */
  public List<CptPitchPath> queryByVertex(int locus, CptPitchNode pitch) {

    return loci.get(locus).stream()
            .filter(path -> Objects.equals(path.getFrom(), pitch))
            .collect(Collectors.toList());
  }

  /**
   * Find CptPitchPaths that go to the specified pitch.
   *
   * @param locus locus.
   * @param pitch specified destination CptPitchNode.
   * @return List of qualified CptPitchPath.
   */
  public List<CptPitchPath> queryByDestination(int locus, CptPitchNode pitch) {

    return loci.get(locus).stream()
            .filter(path -> path.getTo().equals(pitch))
            .collect(Collectors.toList());
  }

  @Override
  public String asGraphviz() {

    var incompleted = loci.size() < cf.length();

    var cluster = IntStream.range(0, loci.size()).mapToObj(i -> String.format(
            """
            \tsubgraph cluster_%d {
            \t\tnode [style=filled];
            %s
            \t\tlabel = \"locus #%d\";
            \t\tcolor=blue
            \t}""",
            i,
            loci.get(i).stream()
                    .map(p -> String.format("\t\t%s_%d[label=\"%s\"];", p.getTo().getPitch().name(), i, p.getTo().getPitch().name()))
                    .distinct()
                    .collect(Collectors.joining("\n")),
            i))
            .collect(Collectors.joining("\n"));
    var body = IntStream.range(1, loci.size()).mapToObj(
            i -> loci.get(i).stream().map(
                    path -> isBlank()
                            ? String.format("\t%s -> %s [ label=<c=%.1f>, penwidth=0.5 ];",
                                    path.getFrom().getName() + "_" + (i - 1),
                                    path.getTo().getName() + "_" + i,
                                    path.getCost())
                            : String.format("\t%s -> %s [ label=<c=%.1f, pher=%.2f>, penwidth=0.5 ];",
                                    path.getFrom().getName() + "_" + (i - 1),
                                    path.getTo().getName() + "_" + i,
                                    path.getCost(),
                                    path.getPheromoneTrail()))
                    .collect(Collectors.joining("\n")))
            .collect(Collectors.joining("\n"));
    return String.format("digraph %s {\n\t%s\n\trankdir=LR;\n\tnode [shape=circle];\n%s\n%s\n}",
            this.getClass().getSimpleName(),
            String.format("// cantus firmus = %s", this.cf.getMelody().toString())
            + (incompleted ? String.format("\n\t// Incompleted Graph (cf.length = %d).", cf.length()) : ""),
            cluster, body);
  }

  @Override
  public String asXML() {

    var doc = DocumentHelper.createDocument();
    doc.setXMLEncoding("UTF-8");
    var root = doc.addElement(this.getClass().getSimpleName());
    IntStream.range(0, this.loci.size()).forEach(i -> {
      var locus = root.addElement("locus").addAttribute("number", "" + i);
      this.loci.get(i).stream().forEach(path -> {
        var edge = locus.addElement("CptPitchPath");
        if (Objects.nonNull(path.getFrom())) {
          edge.addElement("from").setText(path.getFrom().getName());
        }
        edge.addElement("to").setText(path.getTo().getName());
        edge.addElement("cost").setText(String.valueOf(path.getCost()));
        edge.addElement("pheromoneTrail").setText(String.valueOf(path.getPheromoneTrail()));
      });
    });
    var format = OutputFormat.createPrettyPrint();
    format.setExpandEmptyElements(true);
    format.setSuppressDeclaration(true);
    var xmlResult = new StringWriter();
    var writer = new XMLWriter(xmlResult, format);
    try {
      writer.write(doc);
    } catch (IOException ex) {
      Logger.getLogger(CptCfPitchSpace.class.getName()).log(Level.SEVERE, null, ex);
    }
    return xmlResult.toString();
  }

  private boolean hasStylisticDepartures(CptPitchPath path, int locus) {

    var isConsonanceAtLocus = isConsonanceAtLocus(path.getTo(), locus);
    var isForbiddenMotion = isForbiddenMotion(path, locus);
    var isVoiceOverlappingAtLocus = isVoiceOverlappingAtLocus(this.treble, path.getTo(), locus);
    var isVoiceCrossing = isVoiceCrossing(this.treble, path, locus);
    var isUnisonAtLocus = isUnisonAtLocus(path.getTo(), locus);

    var result = !isConsonanceAtLocus || isForbiddenMotion
            || isVoiceOverlappingAtLocus || isVoiceCrossing
            || isUnisonAtLocus;

    return result;
  }

  public boolean isConsonanceAtLocus(CptPitchNode pitch, int locus) {

    var pitch2 = cf.getNote(locus);
    var degree = Math.abs(CptPitch.diatonicDiff(pitch, pitch2));
    var quality = CptPitch.quality(pitch, pitch2);
    return switch (degree % 7) {
      case 0, 4: yield quality.equals(Perfect);
      case 2, 5: yield List.of(Major, Minor).contains(quality);
      default: yield false;
    };
  }

  public boolean isForbiddenMotion(CptPitchPath path, int locus) {

    var cf_path = CptPitchPath.of(cf.getNote(locus - 1), cf.getNote(locus));

    var isForbiddenHiddenParallel
            = CptContrapuntalMotion.isForbiddenHiddenParallel(path, cf_path);
    var isForbiddenParallel
            = CptContrapuntalMotion.isForbiddenParallel(path, cf_path);
    var isForbiddenLeap
            = CptContrapuntalMotion.isForbiddenLeap(path, cf_path);
    var hasDevilAcrossVoices
            = CptContrapuntalMotion.hasDevilAcrossVoices(path, cf_path);

    var result = isForbiddenHiddenParallel || isForbiddenParallel
            || isForbiddenLeap || hasDevilAcrossVoices;

    return result;
  }

  public boolean isVoiceOverlappingAtLocus(boolean treble, CptPitchNode pitch, int locus) {

    var diff = CptPitch.diff(cf.getNote(locus), pitch);
    return diff != 0 && treble ^ diff > 0;
  }

  public boolean isVoiceCrossing(boolean treble, CptPitchPath path, int locus) {

    return isVoiceOverlappingAtLocus(treble, path.getFrom(), locus)
            || isVoiceOverlappingAtLocus(treble, path.getTo(), locus - 1);
  }

  public boolean isUnisonAtLocus(CptPitchNode pitch, int i) {

    return pitch.getPitch().ordinal() == cf.getNote(i).getPitch().ordinal();
  }

  public CptCantusFirmus getCf() {

    return cf;
  }

  public boolean isTreble() {

    return treble;
  }

  public List<Set<CptPitchPath>> getLoci() {

    return loci;
  }

  public boolean isEmpty() {

    return Objects.isNull(this.loci) || this.loci.isEmpty()
            || this.loci.stream().allMatch(Set::isEmpty);
  }

  @Override
  public CptPitchNode getStart() {

    if (this.isEmpty()) {
      throw new EmptyGraphException();
    }
    var starts = this.loci.get(0).stream()
            .map(CptPitchPath::getTo)
            .collect(Collectors.toList());
    var index = getRandom().nextInt(starts.size());
    return starts.get(index);
  }
}
