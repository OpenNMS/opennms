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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

public class GraphMLGraph extends GraphMLElement {

    private final LinkedHashMap<String, GraphMLEdge> edgesById = new LinkedHashMap<>();
    private final LinkedHashMap<String, GraphMLNode> nodeById = new LinkedHashMap<>();

    public void addEdge(GraphMLEdge edge) {
        edgesById.put(edge.getId(), edge);
    }

    public void addNode(GraphMLNode node) {
        nodeById.put(node.getId(), node);
    }

    public Collection<GraphMLEdge> getEdges() {
        return edgesById.values();
    }

    public Collection<GraphMLNode> getNodes() {
        return nodeById.values();
    }

    public GraphMLNode getNodeById(String id) {
        return nodeById.get(id);
    }

    public GraphMLEdge getEdgeById(String id) {
        return edgesById.get(id);
    }

    @Override
    public <T> T accept(GraphMLElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgesById, nodeById);
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            if (obj instanceof GraphMLGraph) {
                GraphMLGraph other = (GraphMLGraph) obj;
                equals = Objects.equals(edgesById, other.edgesById) && Objects.equals(nodeById, other.nodeById);
                return equals;
            }
        }
        return false;
    }
}
