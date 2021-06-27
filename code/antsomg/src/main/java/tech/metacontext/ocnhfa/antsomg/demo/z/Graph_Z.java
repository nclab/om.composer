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
package tech.metacontext.ocnhfa.antsomg.demo.z;

import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;

/**
 *
 * @author Jonathan Chang, Chun-zien <ccz@musicapoetica.org>
 */
public class Graph_Z extends StandardGraph<Edge_Z, Vertex_Z> {

   public Graph_Z(double alpha, double beta) {

      super(alpha, beta);
   }

   @Override
   public void init_graph() {

      var z0 = new Vertex_Z("Start");
      var z1 = new Vertex_Z("z1");
      var z2 = new Vertex_Z("z2");

      this.setStart(z0);

      var z0_z1 = new Edge_Z(z0, z1, 1.0);
      var z1_z2 = new Edge_Z(z1, z2, 1.0);
      var z2_z0 = new Edge_Z(z2, z0, 1.0);

      this.addEdges(
              z0_z1, z0_z1.getReverse(),
              z1_z2, z1_z2.getReverse(),
              z2_z0, z2_z0.getReverse());
   }
}
