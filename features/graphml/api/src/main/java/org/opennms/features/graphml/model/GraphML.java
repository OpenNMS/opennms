/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.graphml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphML extends GraphMLElement {
    private List<GraphMLGraph> graphs = new ArrayList<>();

    public GraphMLGraph getGraph(String id) {
        return graphs.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T> T accept(GraphMLElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public void addGraph(GraphMLGraph graph) {
        this.graphs.add(graph);
    }

    public List<GraphMLGraph> getGraphs() {
        return graphs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), graphs);
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            if (obj instanceof GraphML) {
                return Objects.equals(graphs, ((GraphML) obj).graphs);
            }
        }
        return false;
    }
}

