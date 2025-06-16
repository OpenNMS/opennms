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
package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Graph for test purposes.
 *
 * @author mvrueden
 */
public class TestGraph implements Graph {

    private List<Vertex> vertices;
    private List<Edge> edges;

    private Layout layout;

    public TestGraph(Layout layout, List<Vertex> vertices, List<Edge> edges) {
        this.layout = layout;
        this.vertices = vertices;
        this.edges = edges;
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Collection<Vertex> getDisplayVertices() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public Collection<Edge> getDisplayEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public Edge getEdgeByKey(String edgeKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vertex getVertexByKey(String vertexKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<VertexRef, Status> getVertexStatus() {
        return new HashMap<>();
    }

    @Override
    public Map<EdgeRef, Status> getEdgeStatus() {
        return new HashMap<>();
    }

    @Override
    public void visit(GraphVisitor visitor) throws Exception {
        visitor.visitGraph(this);
        for (Vertex v : vertices) {
            visitor.visitVertex(v);
        }
        for (Edge e : edges) {
            visitor.visitEdge(e);
        }
        visitor.completeGraph(this);
    }

}