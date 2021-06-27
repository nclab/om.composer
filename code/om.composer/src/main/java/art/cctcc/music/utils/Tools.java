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
package art.cctcc.music.utils;

import art.cctcc.music.cpt.ex.ImmatureCptCfPitchSpaceException;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchSpace;
import art.cctcc.music.cpt.graphs.y_cpt.CptPitchSpaceChromatic;
import art.cctcc.music.cpt.graphs.y_cpt_cf.CptCfPitchSpace;
import art.cctcc.music.cpt.model.CptCantusFirmus;
import art.cctcc.music.utils.io.CFXMLReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.LINE;
import tech.metacontext.ocnhfa.composer.cf.utils.Pair;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Tools {

  public static void main(String[] args) {

    if (args.length == 0) {
      System.out.println("CF Checking Tool.\nArgs: 1. folder in /_cf, 2. chromatic (optional).");
      System.exit(0);
    }

    var cf_folder = args[0];
    System.out.println("cf = " + cf_folder);

    var chromatic = args.length > 1 ? args[1].toLowerCase().equals("chromatic") : false;
    System.out.println("chromatic = " + chromatic);

    var folders = new File(System.getProperty("user.dir")).toPath()
            .resolve("_cf").resolve(cf_folder).toFile()
            .listFiles(File::isDirectory);

    Arrays.stream(folders)
            .peek(folder -> System.out.println(LINE + "\n" + folder.getName() + "\n" + LINE))
            .map(folder -> new File(folder, "cantus_firmus"))
            .forEach(cf -> Tools.checkCF(cf, chromatic ? CptPitchSpaceChromatic.getInstance() : null));
  }

  public static void checkCF(File folder, CptPitchSpace cps) {

    var cf_files = folder.listFiles((dir, name) -> name.endsWith(".xml"));
    var cf_list = Arrays.stream(cf_files)
            .map(CFXMLReader::getCptCantusFirmusFromXML)
            .collect(Collectors.toList());

    var bad_list = new TreeMap<CptCantusFirmus, Pair<Boolean>>(Comparator.comparing(CptCantusFirmus::getId));
    cf_list.stream()
            .map(cf -> new SimpleEntry<>(cf, Tools.checkCF(cf, cps)))
            .filter(e -> e.getValue() != null)
            .forEach(e -> bad_list.put(e.getKey(), e.getValue()));
    if (bad_list.isEmpty()) {
      System.out.println(folder.getName() + " test passed.");
    } else {
      System.out.println("Bad cf(s) found:");
      bad_list.forEach((cf, bad) -> {
        System.out.printf("** %s %s %s\n",
                cf.getId(), cf.getMelody(),
                List.of(bad.e1() ? "treble" : "", bad.e2() ? "bass" : "")
                        .toString().replace(", ", ""));
      });
    }
    System.out.println(LINE);
  }

  /**
   * Check if the CptCfPitchSpace is immature based on given cantus firmus.
   *
   * @param cf Given cantus firmus.
   * @return Pair of Boolean if any of treble/bass graphs to be immature.<br>
   * <code>null</code> if both can be mature.
   */
  public static Pair<Boolean> checkCF(CptCantusFirmus cf, CptPitchSpace cps) {

    var cpt_treble = new CptCfPitchSpace(cf, true);
    var cpt_bass = new CptCfPitchSpace(cf, false);
    if (cps != null) {
      cpt_treble.setYCpt(cps);
      cpt_bass.setYCpt(cps);
    }
    boolean treble_bad = false, bass_bad = false;
    try {
      cpt_treble.init_graph();
    } catch (ImmatureCptCfPitchSpaceException ex) {
      treble_bad = true;
    }
    try {
      cpt_bass.init_graph();
    } catch (ImmatureCptCfPitchSpaceException ex) {
      bass_bad = true;
    }
    return (treble_bad || bass_bad)
            ? new Pair(treble_bad, bass_bad) : null;
  }

  public static void deleteFolder(Path project_folder) throws IOException {

    Files.walkFileTree(project_folder, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e)
              throws IOException {
        if (e != null) {
          throw e;
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
