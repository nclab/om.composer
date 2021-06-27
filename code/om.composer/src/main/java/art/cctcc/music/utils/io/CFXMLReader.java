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
package art.cctcc.music.utils.io;

import art.cctcc.music.cpt.ex.CptPitchPathNotFoundException;
import art.cctcc.music.cpt.framework.CptComposer;
import art.cctcc.music.cpt.graphs.x.CptMusicSpace;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.cpt.model.enums.CptEcclesiasticalMode;
import art.cctcc.music.cpt.model.enums.CptPitch;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import tech.metacontext.ocnhfa.composer.cf.model.enums.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CFXMLReader {

  public static CptCantusFirmus getCptCantusFirmusFromXML(File xml_source) {

    var doc = XMLReader(xml_source);
    var cf = new CptCantusFirmus(xml_source.getName().replace(".xml", ""));
    parseMelodyText(doc.getRootElement().element("CantusFirmus").getText())
            .forEach(cf::addNote);
    cf.setMode(CptEcclesiasticalMode.valueOf(doc.getRootElement().elementText("mode")));
    return cf;
  }

  public static CptMusicSpace getCptMusicSpaceFromCFComposer(File xml_source, boolean dupeCost) {

    var root = XMLReader(xml_source).getRootElement();
    assert "CFComposer".equals(root.getName());
    System.out.println("Setting CptMusicSpace...");
    var cptMusicSpace = new CptMusicSpace();
    cptMusicSpace.init_graph();
    root.element("MusicSpace").elements("MusicPath").stream()
            .forEach(xml_path -> {
              var from = MusicThought.getNode(xml_path.element("from").getText());
              var to = MusicThought.getNode(xml_path.element("to").getText());
              var pheromone = Double.valueOf(xml_path.element("pheromoneTrail").getText());
              var path = cptMusicSpace.queryByVertex(from).stream()
                      .filter(p -> Objects.equals(p.getTo(), to))
                      .findFirst().get();
              path.setPheromoneTrail(pheromone);
              if (dupeCost) {
                var cost = Double.valueOf(xml_path.element("cost").getText());
                path.setCost(cost);
              }
            });
    return cptMusicSpace;
  }

  public static CptComposer getCptComposerFromXML(File xml_source) {

    var root = XMLReader(xml_source).getRootElement();
    return getCptComposerFromXML(root);
  }

  public static CptComposer getCptComposerFromXML(Element root) {
    
    assert "CptComposer".equals(root.getName())
            || "Composer".equals(root.getName()) && root.element("cantus_firmus") != null;  //for backward compatibility
    var cf = new CptCantusFirmus(root.element("cantus_firmus").getText());
    cf.setMode(CptEcclesiasticalMode.valueOf(root.element("mode").getText()));
    parseMelodyText(root.element("cantus_firmus_melody").getText())
            .forEach(cf::addNote);
    var treble = "treble".equals(root.element("counterpoint_place").getText())
            || "above".equals(root.element("counterpoint_place").getText()); // for backward compatibility
    var composer = CptComposer.getInstance(root.attributeValue("id"), cf, treble);
    composer.init_graphs();
    System.out.println("Setting MusicSpace...");
    root.element("CptMusicSpace").elements("MusicPath").stream()
            .forEach(xml_path -> {
              var from = MusicThought.getNode(xml_path.element("from").getText());
              var to = MusicThought.getNode(xml_path.element("to").getText());
              var pheromone = Double.valueOf(xml_path.element("pheromoneTrail").getText());
              composer.getX().queryByVertex(from).stream()
                      .filter(p -> Objects.equals(p.getTo(), to))
                      .findFirst().get()
                      .setPheromoneTrail(pheromone);
            });
    System.out.println("Setting CptCfPitchSpace...");
    root.element("CptCfPitchSpace").elements("locus").stream()
            .collect(Collectors.toMap(
                    l -> Integer.valueOf(l.attributeValue("number")),
                    l -> l.elements("CptPitchPath")))
            .entrySet().stream()
            .sorted(Entry.comparingByKey())
            .forEach(entry -> {
              var locus = entry.getKey();
              entry.getValue().stream().forEach(xml_path -> {
                var to = CptPitch.valueOf(xml_path.element("to").getText()).getNode();
                var pheromoneTrail = Double.valueOf(xml_path.element("pheromoneTrail").getText());
                composer.getY().queryByDestination(locus, to).stream()
                        .filter(p -> locus == 0 || Objects.equals(p.getFrom(), CptPitch.valueOf(xml_path.element("from").getText()).getNode()))
                        .findAny().orElseThrow(() -> new CptPitchPathNotFoundException("at locus=" + locus + ", to=" + to))
                        .setPheromoneTrail(pheromoneTrail);
              });
            });
    return composer;
  }

  public static List<CptPitchNode> parseMelodyText(String melody_string) {

    var pitches = melody_string
            .replaceAll("[\\[\\]\\* ]", "")
            .split(",");
    return Stream.of(pitches)
            .map(CptPitch::valueOf)
            .map(CptPitch::getNode)
            .collect(Collectors.toList());
  }

  public static Document XMLReader(File xml_source) {

    try (var fr = new FileReader(xml_source);
            var br = new BufferedReader(fr);) {
      var doc = br.lines().collect(Collectors.joining(" "));
      return DocumentHelper.parseText(doc);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(CFXMLReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException | DocumentException ex) {
      Logger.getLogger(CFXMLReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
