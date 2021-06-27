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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import static java.util.function.Predicate.not;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.initialization;
import tech.metacontext.ocnhfa.composer.cf.model.Parameters;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.createTimeBasedId;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.getParam;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Settings {

  public final String project_name;
  public final long seed;
  public final int composer_no;
  public final int motet_no_per_composer;
  public final String cf_project_folder;
  public final String cf_model_folder;
  public final FractionMode fraction_mode;
  public final boolean test;
  public final boolean parallel;
  public final boolean check_cf;
  public final boolean chromatic; // ICCC

  /**
   * Advanced parameters:<br>
   * SEED=(Long) RANDOM*<br>
   * FRACTION_MODE=Power | Coefficient* | Power_Multiply<br>
   * PARALLEL<br>
   * CHECK_CF
   */
  public static final String HELP = """
          Basic Parameters:
          CF=(String) Cantus Firmus project folder as CF source in /_cf
          MODEL=(String) Model selected from CF project folder
          COMPOSER=(Integer) 3* | Composer number, decided by folder no, if MODEL not specified
          MOTET=(Integer) 10* (2 in TEST mode) | Motet number per composer
          TEST
          CHROMATIC (for ICCC)
          *: default
          """;

  public Settings(Map<String, String> params) {

    this.test = getParam(params, "TEST", false, v -> true);

    this.check_cf = getParam(params, "CHECK_CF", false, v -> true);

    this.chromatic = getParam(params, "CHROMATIC", false, v -> true); //ICCC

    this.project_name = "ORGANUM.MOTET_" + (this.test ? "TEST" : createTimeBasedId())
            + (this.chromatic ? "_CHR" : ""); //ICCC

    this.seed = getParam(params, "SEED", System.currentTimeMillis(),
            value -> "RANDOM".equals(value) ? System.currentTimeMillis()
            : Long.valueOf(value));

    initialization(this.seed);

    this.parallel = getParam(params, "PARALLEL", false, v -> true);

    this.fraction_mode
            = getParam(params, "FRACTION_MODE", Parameters.DEFAULT_FRACTION_MODE,
                    FractionMode::valueOf);

    this.motet_no_per_composer
            = getParam(params, "MOTET", test ? 2 : DEFAULT_MOTET_NO_PER_COMPOSER,
                    Integer::valueOf);

    this.cf_project_folder = getParam(params, "CF", DEFAULT_CF,
            String::valueOf);

    var cf_model_folder_no = new File(CF_DIR, cf_project_folder)
            .listFiles(File::isDirectory).length;

    this.composer_no = params.containsKey("MODEL")
            ? getParam(params, "COMPOSER", DEFAULT_COMPOSER_NO,
                    Integer::valueOf)
            : (cf_model_folder_no == 1 ? DEFAULT_COMPOSER_NO
                    : cf_model_folder_no);

    this.cf_model_folder = getParam(params, "MODEL", "",
            suffix -> new File(CF_DIR, cf_project_folder)
                    .listFiles(path -> path.isDirectory() && path.toString().endsWith(suffix))[0]
                    .getName());
  }

  public Settings(Properties props) {

    project_name = props.getProperty("ProjectName");
    seed = Long.valueOf(props.getProperty("SEED"));
    composer_no = Integer.valueOf(props.getProperty("COMPOSER"));
    motet_no_per_composer = Integer.valueOf(props.getProperty("MOTET"));
    cf_project_folder = props.getProperty("CF");
    cf_model_folder = props.getProperty("MODEL");
    fraction_mode = FractionMode.valueOf(props.getProperty("FRACTION_MODE"));
    test = Boolean.valueOf(props.getProperty("TEST"));
    parallel = Boolean.valueOf(props.getProperty("PARALLEL"));
    check_cf = Boolean.valueOf(props.getProperty("CHECK_CF"));
    chromatic = Boolean.valueOf(props.getProperty("CHROMATIC")); //ICCC
  }

  public Path saveSetting() {

    var project_folder = Path.of(art.cctcc.music.Parameters.PROJECT_DIR.getPath(), project_name);
    try {
      if (test && project_folder.toFile().exists()) {
        System.out.println("Deleting previous test output...");
        Tools.deleteFolder(project_folder);
      }
      project_folder.toFile().mkdirs();
      var props = new Properties();
      props.put("ProjectName", project_name);
      props.put("SEED", "" + seed);
      props.put("COMPOSER", "" + composer_no);
      props.put("MOTET", "" + motet_no_per_composer);
      props.put("CF", cf_project_folder);
      props.put("MODEL", cf_model_folder);
      props.put("FRACTION_MODE", "" + fraction_mode);
      props.put("TEST", "" + test);
      props.put("PARALLEL", "" + parallel);
      props.put("CHECK_CF", "" + check_cf);
      props.put("CHROMATIC", "" + chromatic); //ICCC
      try (var os = new FileOutputStream(project_folder.resolve(SETTINGS_FILENAME).toFile())) {
        props.storeToXML(os, toString());
      }
    } catch (IOException ex) {
      Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
    }
    return project_folder;
  }

  @Override
  public String toString() {

    var output = String.format("""
            ProjectName=%s
            SEED=%s
            COMPOSER=%s
            MOTET=%s
            CF=%s
            MODEL=%s
            FRACTION_MODE=%s""",
            project_name, seed, composer_no, motet_no_per_composer,
            cf_project_folder, cf_model_folder, fraction_mode);
    if (test || parallel || chromatic) {
      output += "\n" + Stream.of(
              test ? "TEST" : "",
              parallel ? "PARALLEL" : "",
              check_cf ? "CHECK_CF" : "",
              chromatic ? "CHROMATIC" : "" //ICCC
      )
              .filter(not(String::isBlank))
              .collect(Collectors.joining(" "));
    }
    return output;
  }
}
