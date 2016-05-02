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

package org.opennms.features.topology.plugins.topo.graphml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SimpleSearchProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphML;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;

public class GraphMLSearchProvider extends SimpleSearchProvider {

    private Optional<GraphML> getGraph() {
        return Optional.ofNullable(GraphMLTopologyProvider.lastGraph);
    }

    @Override
    public String getSearchProviderNamespace() {
        return (String) getGraph().map(g -> g.getProperty(GraphMLProperties.NAMESPACE)).orElse(null);
    }

    @Override
    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        // Search all of the nodes on all the graphs
        final List<GraphMLVertex> matchingVertices = new ArrayList<>();
        getGraph().ifPresent(d -> {
            d.getGraphs().stream()
                .map(g -> g.getNodes())
                .flatMap(l -> l.stream())
                .filter(n -> matches(searchQuery, n))
                .sorted((n1, n2) -> n1.getId().compareTo(n2.getId()))
                .map(n -> new GraphMLVertex(n))
                .forEach(v -> matchingVertices.add(v));
        });
        return matchingVertices;
    }

    /**
     * Returns true if either if either the graph node's id, or the values of any
     * of the graph node's properties contain the query string.
     */
    private static boolean matches(SearchQuery searchQuery, GraphMLNode graphMLNode) {
        final String qs = searchQuery.getQueryString().toLowerCase();
        for (Object propValue : graphMLNode.getProperties().values()) {
            final String value = propValue != null ? propValue.toString() : "";
            if (value.toLowerCase().contains(qs)) {
                return true;
            }
        }
        return graphMLNode.getId().toLowerCase().contains(qs);
    }
}
