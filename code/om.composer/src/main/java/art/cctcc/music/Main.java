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
package art.cctcc.music;

import art.cctcc.music.motet.MotetStudio;
import art.cctcc.music.utils.Settings;
import java.io.IOException;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.LINE;
import static tech.metacontext.ocnhfa.composer.cf.model.Parameters.argsToParam;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Main {

  public static void main(String... args) throws IOException {

    System.out.println(LINE);
    System.out.println("Organum Motet Composer For The First Species Counterpoint");
    System.out.println(LINE);

    var params = argsToParam(args);

    if (params.isEmpty()) {
      System.out.println(Settings.HELP);
      System.exit(0);
    }

    var settings = new Settings(params);
    System.out.printf("\n%s\n%s\n%s\n", LINE, "Settings", LINE);
    System.out.println(settings);

    var project_folder = settings.saveSetting();

    System.out.printf("\n%s\n%s\n%s\n", LINE, "Initializing Motet Studio...", LINE);
    var studio = new MotetStudio(project_folder);

    System.out.printf("\n%s\n%s\n%s\n", LINE, "Developing scheme planners...", LINE);
    studio.developSchemePlanners();

    System.out.printf("\n%s\n%s\n%s\n", LINE, "Planning schemes...", LINE);
    studio.planSchemes();
    studio.saveSchemes();

    System.out.printf("\n%s\n%s\n%s\n", LINE, "Composing motets...", LINE);
    studio.composeMotets();

    System.out.printf("\n%s\n%s\n", LINE, "Saving...");
    studio.saveMotets();
  }
}
