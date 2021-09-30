/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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