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
package tech.metacontext.ocnhfa.antsomg.demo.y;

import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Graph_Y extends StandardGraph<Edge_Y, Vertex_Y> {

   public Graph_Y(double alpha, double beta) {

      super(alpha, beta);
   }

   @Override
   public void init_graph() {
      var y0 = new Vertex_Y("Start");
      var y1 = new Vertex_Y("y1");
      var y2 = new Vertex_Y("y2");

      this.setStart(y0);

      var y0_y1 = new Edge_Y(y0, y1, 1.0);
      var y1_y2 = new Edge_Y(y1, y2, 1.0);
      var y2_y0 = new Edge_Y(y2, y0, 1.0);
      this.addEdges(
              y0_y1, y0_y1.getReverse(),
              y1_y2, y1_y2.getReverse(),
              y2_y0, y2_y0.getReverse());
   }
}
