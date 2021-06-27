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
package art.cctcc.music.cpt.ex;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class EmptyGraphException extends RuntimeException {

  /**
   * Creates a new instance of <code>EmptyGraphException</code> without detail
   * message.
   */
  public EmptyGraphException() {
    
  }

  /**
   * Constructs an instance of <code>EmptyGraphException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   */
  public EmptyGraphException(String msg) {

    super(msg);
  }
}
