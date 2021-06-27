/*
 * Copyright 2020 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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

import java.io.File;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Parameters {

  public static final File PROJECT_DIR
          = new File(System.getProperty("user.dir"), "_projects");
  public static final File CF_DIR
          = new File(System.getProperty("user.dir"), "_cf");
  public static final File CF_MODEL_DIR
          = new File(CF_DIR, "_model");

  public static final String SETTINGS_FILENAME
          = "settings.xml";
  public static final String DEFAULT_CF
          = "GENERATE_CF_02-18-58-824635800";
  
  public static double ALPHA = 1.0;
  public static double BETA = 1.0;
  public static double MUTATION_RATE = 0.2;
  public static double CROSSOVER_RATE = 0.1;

  public static final int PLANNER_DEVELOPING_POPULATION = 1000;
  public static final int PLANNER_PLANNING_POPULATION = 1;
  public static final int PLANNER_TESTING_POPULATION = 10;
  public static final int CPT_DEVELOPING_POPULATION = 10000;
  public static final int CPT_COMPOSING_POPULATION = 100;
  public static final int CPT_TESTING_POPULATION = 20;

  public static double X_PHEROMONE_EVAPORATE_RATE = 0.1;
  public static double X_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
  public static double X_ALPHA = 2.0, X_BETA = 1.0;
  public static double X_EXPLORE_CHANCE = 0.1;

  public static double Y_PHEROMONE_EVAPORATE_RATE = 0.1;
  public static double Y_PHEROMONE_DEPOSIT_AMOUNT = 1.0;
  public static double Y_ALPHA = 1.0, Y_BETA = 1.0;
  public static double Y_EXPLORE_CHANCE = 0.2;

  public static final double COST_STANDARD = 1.0;
  public static final double COST_CPT_TO_CF = 3.0;
  public static final double COST_CPT_TO_FINISH = 5.0;

  public static final int MAX_SECTION_NUMBER = 9;
  public static final int MIN_SECTION_NUMBER = 6;
  public static final int MIN_CF_NUMBER = 2;

  public static final int DEFAULT_COMPOSER_NO = 3;
  public static final int DEFAULT_MOTET_NO_PER_COMPOSER = 10;

  public static final double MOTET_COMPOSE_THRESHOLD = 0.95;
  public static final double MOTET_COMPOSE_THRESHOLD_TEST = 0.88;
  public static final int MOTET_COMPOSE_GENERATION = 120;
  public static final int MOTET_COMPOSE_TARGET_SIZE = 20;

  //CF Generator
  public static String DEFAULT_EC_MODEL
          = "DEVELOP_MODAL_STANDARD_04-21-00-700950200";
  public static int DEFAULT_GENERATION = 300;
  public static int DEFAULT_POPULATION_SIZE = 400;
  public static int DEFAULT_TARGET_SIZE = 100;
}
