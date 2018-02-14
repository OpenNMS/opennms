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

