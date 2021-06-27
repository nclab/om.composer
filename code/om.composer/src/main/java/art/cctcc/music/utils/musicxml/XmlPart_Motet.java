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
package art.cctcc.music.utils.musicxml;

import art.cctcc.music.cpt.model.CptMelody;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class XmlPart_Motet {

  private final String id;
  private final String part_name;
  private final String part_abbreviation;
  private final XmlClef clef;
  private final List<CptMelody> melodies;

  public XmlPart_Motet(String id,
          String part_name,
          String part_abbreviation,
          XmlClef clef,
          CptMelody... melodies) {

    this.id = id;
    this.part_name = part_name;
    this.part_abbreviation = part_abbreviation;
    this.clef = clef;
    this.melodies = new ArrayList<>(Arrays.asList(melodies));
  }

  public String getId() {

    return id;
  }

  public String getPart_name() {

    return part_name;
  }

  public String getPart_abbreviation() {

    return part_abbreviation;
  }

  public XmlClef getClef() {

    return clef;
  }

  public List<CptMelody> getMelodies() {

    return this.melodies;
  }

}
