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
package tech.metacontext.ocnhfa.composer.cf;

import java.util.Map;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import static tech.metacontext.ocnhfa.antsomg.impl.StandardParameters.initialization;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.*;
import tech.metacontext.ocnhfa.composer.cf.model.Studio;
import tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType;
import static tech.metacontext.ocnhfa.composer.cf.model.enums.ComposerType.*;
import tech.metacontext.ocnhfa.composer.cf.model.enums.EcclesiasticalMode;
import tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml.Clef;

/**
 * Cantus Firmus Composer for Species Counterpoint
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Main {

  static Map<String, String> params;

  /**
   * main entry of cf.composer.
   *
   * @param args
   */
  public static void main(String[] args) {

    params = argsToParam(args);

    if (params.isEmpty()) {
      System.out.println(HELP);
      System.exit(0);
    }
    System.out.println(LINE);

    var type = getParam(params, "TYPE",
            COMPOSE_STATIC, ComposerType::valueOfIgnoreCase);

    initialization(getParam(params, "SEED", DEFAULT_SEED, value -> switch (value) {
      case "RANDOM" ->
        System.currentTimeMillis() / 1000;
      default ->
        Long.valueOf(value);
    }));

    var thread_number = getParam(params, "THREAD_NUMBER",
            type == DEVELOP_STANDARD || type == DEVELOP_MODAL_STANDARD
                    ? DEFAULT_DEVELOP_THREAD_NUMBER : DEFAULT_COMPOSE_THREAD_NUMBER,
            Integer::valueOf);

    var target_size = getParam(params, "TARGET_SIZE",
            DEFAULT_TARGET_SIZE, Integer::valueOf);

    var fraction_mode = getParam(params, "FRACTION_MODE",
            DEFAULT_FRACTION_MODE, FractionMode::valueOf);

    var clef = getParam(params, "CLEF", null, Clef::valueOf);

    var save = getParam(params, "SAVE", false, Boolean::valueOf);

    var studio = new Studio(type)
            .setThread_number(thread_number)
            .setTarget_size(target_size)
            .setFraction_mode(fraction_mode);

    switch (type) {
      case DEVELOP_STANDARD -> {
        var composer_number = getParam(params, "COMPOSER_NUMBER",
                DEFAULT_COMPOSER_NUMBER, Integer::valueOf);
        var ecclesiastical_mode = getParam(params, "MODE",
                EcclesiasticalMode.RANDOM_MODE, EcclesiasticalMode::valueOf);
        studio.setComposer_number(composer_number)
                .setEcclesiastical_Mode(ecclesiastical_mode);
      }
      case COMPOSE, COMPOSE_STATIC -> {
        var project = getParam(params, "PROJECT", DEMO_STANDARD, String::valueOf);
        var folder = getParam(params, "FOLDER", null, String::valueOf);
        studio.setModel(project, folder);
      }
    }

    studio.run();

    if (save) {
      studio.saveScore(clef);
    }
  }

  private static final String HELP = String.format(
          """
          %s
          Cantus Firmus Composer for Species Counterpoint
          %s
          (* default)
          
          TYPE=DEVELOP_STANDARD | DEVELOP_MODAL_STANDARD | COMPOSE | COMPOSE_STATIC*
          SEED=(Long) random seed | RANDOM | DEFAULT_SEED*
          THREAD_NUMBER=(Integer) number of threads | 
          TARGET_SIZE=(Integer) selected target size
          FRACTION_MODE=Power | Coefficient | Power_Multiply
          CLEF=Treble | Bass | Soprano | Tenor | Alto (auto select by range if not specified)
          SAVE=(Boolean) Specify if save score, FALSE by default)
          %s
          When TYPE=DEVELOP_STANDARD
          COMPOSER_NUMBER=(Integer) number of composer(s)
          MODE=Dorian | Phrygian | Mixolydian | Aeolian | Ionian | RANDOM_MODE*
          %s
          When TYPE=COMPOSE | COMPOSE_STATIC
          PROJECT=(String) specified project folder as preset
          FOLDER=(String) a prefix for sub-folders in the specified project folder as preset
          """, LINE, LINE, LINE, LINE);
}
