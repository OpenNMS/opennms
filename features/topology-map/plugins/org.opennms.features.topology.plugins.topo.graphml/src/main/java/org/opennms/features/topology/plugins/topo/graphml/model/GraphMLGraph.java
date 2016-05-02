/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphMLGraph extends GraphMLElement {

    private List<GraphMLEdge> edges = new ArrayList<>();

    private List<GraphMLNode> nodes = new ArrayList<>();

    public void addEdge(GraphMLEdge edge) {
        edges.add(edge);
    }

    public void addNode(GraphMLNode node) {
        nodes.add(node);
    }

    public List<GraphMLEdge> getEdges() {
        return edges;
    }

    public List<GraphMLNode> getNodes() {
        return nodes;
    }

    public GraphMLNode getNodeById(String id) {
        return getNodes().stream().filter(node -> node.getId().equals(id)).findFirst().orElse(null);
    }

    public GraphMLEdge getEdgeById(String id) {
        return getEdges().stream().filter(edge -> edge.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public <T> T accept(GraphMLElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edges, nodes);
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            if (obj instanceof GraphMLGraph) {
                GraphMLGraph other = (GraphMLGraph) obj;
                equals = Objects.equals(edges, other.edges) && Objects.equals(nodes, other.nodes);
                return equals;
            }
        }
        return false;
    }
}
