/*
 * Copyright 2021 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use thread file except in compliance with the License.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.composer.cf.model.Composer;
import tech.metacontext.ocnhfa.composer.cf.model.MusicThread;
import tech.metacontext.ocnhfa.composer.cf.model.constraints.MusicThreadRating;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicWriter {

   /**
    *
    * @param project_dir
    * @param composer
    * @return composer folder in File object.
    */
   public synchronized static File saveComposer(String project_dir, Composer composer) {

      File parent = new File(project_dir, "Composer-" + composer.getId());
      parent.mkdirs();
      // write composer.xml
      try ( var fw_composer = new FileWriter(new File(parent, "composer.xml"));
               var bw_composer = new BufferedWriter(fw_composer);) {
         bw_composer.write(composer.asXML());
      } catch (IOException ex) {
         composer.getLogger().log(Level.SEVERE, null, ex);
      }
      // write graph_x.graphviz
      try ( var fw_graph_x = new FileWriter(new File(parent, "graph_x.graphviz"));
               var bw_graph_x = new BufferedWriter(fw_graph_x);) {
         bw_graph_x.write(composer.getX().asGraphviz());
      } catch (IOException ex) {
         composer.getLogger().log(Level.SEVERE, null, ex);
      }
      // write graph_y.graphviz
      try ( var fw_graph_y = new FileWriter(new File(parent, "graph_y.graphviz"));
               var bw_graph_y = new BufferedWriter(fw_graph_y);) {
         bw_graph_y.write(composer.getY().asGraphviz());
      } catch (IOException ex) {
         composer.getLogger().log(Level.SEVERE, null, ex);
      }
      return parent;
   }

   public static void saveRoute(File file, MusicThread thread) {

      var doc = DocumentHelper.createDocument();
      var root = doc.addElement("MusicThread");
      root.addElement("mode")
              .addText(thread.getCf().getEcclesiastical_Mode().name());
      root.addElement("CantusFirmus")
              .addText(thread.getCf().toString());
      root.addElement("middle")
              .addText(thread.getCf().getMiddle().name());
      var rating = root.addElement("rating")
              .addAttribute("rate", String.valueOf(MusicThreadRating.rate(thread)));
      rating.addElement("range")
              .addText(String.valueOf(MusicThreadRating.range(thread)));
      rating.addElement("length")
              .addText(String.valueOf(MusicThreadRating.length(thread)));
      rating.addElement("dominantCount")
              .addText(String.valueOf(MusicThreadRating.dominantCount(thread)));
      var history = root.addElement("history")
              .addAttribute("length", String.valueOf(thread.getCf().length()));
      IntStream.range(1, thread.getCf().length())
              .forEach(i -> {
                 var ph = thread.getCf().getHistory().get(i);
                 var pitch_route = history.addElement("PitchHistory")
                         .addAttribute("move", String.valueOf(i));
                 pitch_route.addElement("to")
                         .addText(ph.getSelected().getTo().getName());
                 pitch_route.addElement("MusicThought")
                         .addText(ph.getMt().name());
                 pitch_route.addElement("exploit")
                         .addText(String.valueOf(!ph.isExploring()));
                 var routes = pitch_route.addElement("routes");
                 ph.getPheromoneRecords().entrySet().stream()
                         .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                         .forEach(entry -> {
                            var route = routes.addElement("route");
                            route.addElement("to").addText(entry.getKey().getTo().getName());
                            route.addElement("pheromoneTrail").addText(String.valueOf(entry.getValue()));
                         });
              });
      synchronized (MusicWriter.class) {
         try ( var fw = new FileWriter(file);
                  var bw = new BufferedWriter(fw);) {
            bw.write(doc.asXML());
         } catch (IOException ex) {
            Logger.getLogger(MusicThread.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }
}
