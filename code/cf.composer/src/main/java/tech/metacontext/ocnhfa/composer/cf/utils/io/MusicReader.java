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
package tech.metacontext.ocnhfa.composer.cf.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.composer.cf.model.Studio;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Clef;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Score;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicReader {

  public synchronized static Document getComposerXML(File folder) {

    var doc = getXMLFromFile(new File(folder, "composer.xml"));
    var root = doc.getRootElement();
    assert List.of("CFComposer", "Composer").contains(root.getName()); //for backward compatibility
    assert root.element("MusicSpace") != null && root.element("PitchSpace") != null;
    return doc;
  }

  public synchronized static Document getXMLFromFile(File xml_source) {

    try ( var fr = new FileReader(xml_source);
             var br = new BufferedReader(fr)) {
      System.out.println("Reading " + xml_source.getPath() + " ...");
      var doc = br.lines().collect(Collectors.joining(" "));
      return DocumentHelper.parseText(doc);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException | DocumentException ex) {
      Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public synchronized static List<Score> threadXmlToMusicXml(File folder, String source) {

    File[] xml_sources = folder.listFiles((dir, name) -> name.endsWith(".xml"));
    return Arrays.stream(xml_sources)
            .map(xml_source -> Map.entry(xml_source.getName(), getXMLFromFile(xml_source)))
            .map(entry -> {
              var melody = entry.getValue().getRootElement()
                      .elementText("CantusFirmus")
                      .replaceAll("[\\[\\]\\* ]", "")
                      .split(",");
              var score = new Score(entry.getKey(), source);
              score.setTempo(480);
              score.setLongerEnding(true);
              score.addMeasure(Clef.Alto, 1,
                      Arrays.stream(melody)
                              .map(Pitch::valueOf)
                              .map(Pitch::getNode).collect(Collectors.toList())
              );
              return score;
            })
            .collect(Collectors.toList());
  }
}
