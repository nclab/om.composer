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
package tech.metacontext.ocnhfa.antsomg.demo.x;

import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Graph_X extends StandardGraph<Edge_X, Vertex_X> {

   @Override
   public void init_graph() {

      var x0 = new Vertex_X("Start");
      var x1 = new Vertex_X("x1");
      var x2 = new Vertex_X("x2");

      this.setStart(x0);

      var x0_x1 = new Edge_X(x0, x1, 1.0);
      var x1_x2 = new Edge_X(x1, x2, 1.0);
      var x2_x3 = new Edge_X(x2, x0, 1.0);

      this.addEdges(
              x0_x1, x0_x1.getReverse(),
              x1_x2, x1_x2.getReverse(),
              x2_x3, x2_x3.getReverse());
   }

}
