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
package tech.metacontext.ocnhfa.antsomg.impl;

import java.util.Objects;
import java.util.Random;
import tech.metacontext.ocnhfa.antsomg.impl.ex.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class StandardParameters {

  private static Random RANDOM;

  public static double ALPHA = 1.0;
  public static double BETA = 1.0;
  public static double PHEROMONE_DEPOSIT = 0.1;
  public static double EXPLORE_CHANCE = 0.1;
  public static double EVAPORATE_RATE = 0.05;

  public static void initialization(long SEED) {

    if (Objects.nonNull(StandardParameters.RANDOM)) {
      throw new RandomSeedInitializedException();
    }
    StandardParameters.RANDOM = new Random(SEED);
  }

  public static Random getRandom() {

    if (Objects.nonNull(StandardParameters.RANDOM)) {
      return StandardParameters.RANDOM;
    }
    throw new RandomSeedNotInitializedException();
  }
}
