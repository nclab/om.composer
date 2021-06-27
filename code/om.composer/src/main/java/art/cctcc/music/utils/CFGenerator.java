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

import static art.cctcc.music.Parameters.*;
import art.cctcc.music.utils.ec.CF_Evaluator;
import art.cctcc.music.utils.ec.CF_Mutator;
import art.cctcc.music.utils.ec.CF_Recombinator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.initialization;
import tech.metacontext.ocnhfa.composer.cf.ec.ECStudio;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.LINE;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.argsToParam;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.createTimeBasedId;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.getParam;
import tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Clef;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CFGenerator {

  /**
   * Cantus firmus generator.
   *
   * @param args <br><code>THRESHOLD</code> Threshold for the outcome;
   * <code>null</code> if not specified.<br>
   * <code>GENERATION</code> Generation; max generation if specified threshold
   * not achieved.<br>
   * <code>POPULATION</code> Population size for EC.<br>
   * <code>TARGET_SIZE</code> Target size (for further selections).<br>
   * <code>MODEL</code> CF Model for EC in <code>/_cf/_model</code> folder.<br>
   * <code>FOLDER</code> Specified folder in source; <code>null</code> if not
   * specified.<br>
   *
   * @throws java.io.IOException
   */
  public static void main(String... args) throws IOException {

    System.out.println(LINE);
    System.out.println("Cantus Firmus Generator for Motet Composer");
    System.out.println(LINE);

    var params = argsToParam(args);

    if (params.isEmpty()) {
      System.out.printf("""
                        SEED=(long) RANDOM*
                        POPULATION=(int) %d*
                        TARGET_SIZE=(int) %d*
                        GENERATION=(int) %d*
                        THRESHOLD=(double) N/A*
                        MODEL=(String) %s*
                        FOLDER=(String) N/A*
                        * Default Values
                        """,
              DEFAULT_POPULATION_SIZE,
              DEFAULT_TARGET_SIZE,
              DEFAULT_GENERATION,
              DEFAULT_EC_MODEL);
      System.exit(0);
    }
    var target_folder = CFGenerator.saveSetting(params);
    generate_ec(target_folder);
  }

  public static Path saveSetting(Map<String, String> params) {

    var target_folder = CF_DIR.toPath().resolve("GENERATE_CF_" + createTimeBasedId());
    try {
      if (target_folder.toFile().exists()) {
        System.out.println("Deleting previous test output...");
        Tools.deleteFolder(target_folder);
      }
      target_folder.toFile().mkdirs();

      var seed = getParam(params, "SEED", System.currentTimeMillis(),
              value -> "RANDOM".equals(value) ? System.currentTimeMillis()
              : Long.valueOf(value));
      initialization(seed);

      var model = getParam(params, "MODEL", DEFAULT_EC_MODEL, String::valueOf);

      var props = new Properties();
      props.put("ProjectName", target_folder.toString());
      props.put("SEED", seed.toString());
      props.put("POPULATION", getParam(params, "POPULATION", DEFAULT_POPULATION_SIZE, Integer::valueOf).toString());
      props.put("TARGET_SIZE", getParam(params, "TARGET_SIZE", DEFAULT_TARGET_SIZE, Integer::valueOf).toString());
      props.put("GENERATION", getParam(params, "GENERATION", DEFAULT_GENERATION, Integer::valueOf).toString());
      props.put("THRESHOLD", getParam(params, "THRESHOLD", "", Double::valueOf).toString());
      props.put("MODEL", model);
      props.put("FOLDER", getParam(params, "FOLDER", "",
              suffix -> new File(CF_MODEL_DIR, model)
                      .listFiles(path -> path.isDirectory() && path.toString().endsWith(suffix))[0]
                      .getName()));
      var settings_file = target_folder.resolve(SETTINGS_FILENAME).toFile();
      try (var os = new FileOutputStream(settings_file)) {
        System.out.println("Saving settings to " + settings_file);
        var comment = "\n" + props.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue() + "\n")
                .collect(Collectors.joining());
        props.storeToXML(os, comment);
      }
    } catch (IOException ex) {
      Logger.getLogger(CFGenerator.class.getName()).log(Level.SEVERE, null, ex);
    }
    return target_folder;
  }

  /**
   * Generate cantus firmus from composer models with EC selection.
   *
   * @param target_folder
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */
  public static void generate_ec(Path target_folder) throws FileNotFoundException, IOException {

    var settings_file = target_folder.resolve(SETTINGS_FILENAME).toFile();
    var props = new Properties();
    try (var in = new FileInputStream(settings_file)) {
      props.loadFromXML(in);
    }
    var population_size = Integer.valueOf(props.getProperty("POPULATION"));
    var target_size = Integer.valueOf(props.getProperty("TARGET_SIZE"));
    var generation = Integer.valueOf(props.getProperty("GENERATION"));
    var threshold = props.getProperty("THRESHOLD").isBlank()
            ? null : Double.valueOf(props.getProperty("THRESHOLD"));
    var source = props.getProperty("MODEL");
    var folder = props.getProperty("FOLDER").isBlank()
            ? null : props.getProperty("FOLDER");

    var cf_studio = new ECStudio(
            ComposerType.COMPOSE_STATIC,
            threshold, generation,
            new CF_Evaluator(), new CF_Recombinator(), new CF_Mutator());

    cf_studio // composer_number is from model.
            .setThread_number(population_size)
            .setTarget_size(target_size)
            .setFraction_mode(StandardGraph.FractionMode.Coefficient);

    System.out.println("Population = " + cf_studio.getThread_number()); //0
    System.out.println("Target size = " + cf_studio.getTarget_size());   //20
    System.out.println("Fraction mode = " + cf_studio.getFraction_mode()); //null

    cf_studio.setProject_dir(CF_MODEL_DIR);
    cf_studio.setModel(source, folder);
    cf_studio.setBase_dir(target_folder.toFile());
    cf_studio.run();

    cf_studio.saveScore(Clef.Alto);
  }
}
