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

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class XmlClef {

  public static XmlClef G2 = new XmlClef(Sign.G, 2);
  public static XmlClef C3 = new XmlClef(Sign.C, 3);
  public static XmlClef F4 = new XmlClef(Sign.F, 4);
  public static XmlClef C4 = new XmlClef(Sign.C, 4);

  public enum Sign {

    G, F, C
  }

  private final Sign sign;
  private final int line;

  public Sign getSign() {

    return sign;
  }

  public int getLine() {

    return line;
  }

  private XmlClef(Sign sign, int line) {

    this.sign = sign;
    this.line = line;
  }
}
