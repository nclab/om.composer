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
package tech.metacontext.ocnhfa.composer.cf.model;

import java.io.File;
import java.time.LocalTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class Parameters {

  public static long DEFAULT_SEED = -1410263943012662127L;
  public static FractionMode DEFAULT_FRACTION_MODE = FractionMode.Coefficient;
  public static final String LINE = "-".repeat(80);

  public static int DEFAULT_DEVELOP_THREAD_NUMBER = 100000;
  public static int DEFAULT_COMPOSE_THREAD_NUMBER = 1000;
  public static int DEFAULT_COMPOSER_NUMBER = 5;
  public static int CF_LENGTH_LOWER = 7;
  public static int CF_LENGTH_HIGHER = 15;
  public static int CF_RANGE_LOWER = 5;
  public static int CF_RANGE_HIGHER = 6;

  public static double DEFAULT_COST = 1.0;
  public static double DOMINANT_ATTRACTION_FACTOR = 10.0;
  public static int DOMINANT_COUNT = 3;
  public static int DEFAULT_TARGET_SIZE = 20;

  public static double X_PHEROMONE_EVAPORATE_RATE = 0.1;
  public static double X_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
  public static double X_ALPHA = 2.0, X_BETA = 1.0;
  public static double X_EXPLORE_CHANCE = 0.2;

  public static double Y_PHEROMONE_EVAPORATE_RATE = 0.1;
  public static double Y_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
  public static double Y_ALPHA = 1.0, Y_BETA = 1.0;
  public static double Y_EXPLORE_CHANCE = 0.4;

  public static String DEMO_STANDARD = "STANDARD_project_00-48-00-571338900";
  public static final File PROJECT_DIR = new File(System.getProperty("user.dir"), "projects");
  public static final File TEST_DIR = new File(System.getProperty("user.dir"), "test");
  public static final File LOG_DIR = new File(System.getProperty("user.dir"), "log");

  // EC
  public static int SELECT_DIV = 4;
  public static int DEFAULT_GENERATION = 300;
  public static int DEFAULT_POPULATION_SIZE = 400;
  public static double DEFAULT_THRESHOLD = 1000000.0;
  public static double DEFAULT_CROSSOVER_RATE = 0.75;
  public static double DEFAULT_MUTATION_RATE = 0.5;

  public static String createTimeBasedId() {

    return LocalTime.now().toString().replaceAll("[:.]", "-");
  }

  public static Map<String, String> argsToParam(String[] args) {

    var params = Stream.of(args)
            .map(arg -> arg.split("="))
            .collect(Collectors.toMap(
                    s -> s[0].toUpperCase(),
                    s -> s.length == 2 ? s[1] : ""));
    return params;
  }

  public static <T> T getParam(
          Map<String, String> params,
          String key,
          T default_value,
          Function<String, T> function) {

    T value = params.containsKey(key)
            ? function.apply(params.remove(key))
            : default_value;
    System.out.println("*** getParam: " + key + "=" + value);
    return value;
  }
}
