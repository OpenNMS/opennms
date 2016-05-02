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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphML;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * TODO: Most of this is copied from the BusinessServiceSearchProvider
 * 
 * @author jwhite
 */
public class GraphMLSearchProvider extends AbstractSearchProvider implements SearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GraphMLSearchProvider.class);

    @Override
    public String getSearchProviderNamespace() {
        return (String) getGraph().map(g -> g.getProperty(GraphMLProperties.NAMESPACE)).orElse(null);
    }

    @Override
    public boolean contributesTo(String namespace) {
        final String graphNamespace = getSearchProviderNamespace();
        return graphNamespace != null ? graphNamespace.equalsIgnoreCase(namespace) : false;
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        final String graphNamespace = getSearchProviderNamespace();
        final String graphPrefix = graphNamespace != null ? graphNamespace : "";
        return supportsPrefix(graphPrefix + "=", searchPrefix);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer container) {
        LOG.info("GraphMLSearchProvider->query: called with search query: '{}'", searchQuery);
        final List<SearchResult> results = Lists.newArrayList();

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

        // Build search results from the matching vertices
        matchingVertices.stream().forEach(v -> {
            SearchResult searchResult = new SearchResult(v);
            searchResult.setCollapsed(false);
            searchResult.setCollapsible(true);
            results.add(searchResult);
        });

        LOG.info("GraphMLSearchProvider->query: found {} results: {}", results.size(), results);
        return results;
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

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        VertexRef vertexToFocus = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        return Sets.newHashSet(vertexToFocus);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("GraphMLSearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.addCriteria(criterion);

        LOG.debug("GraphMLSearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        LOG.debug("GraphMLSearchProvider->addVertexHop: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("GraphMLSearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.removeCriteria(criterion);

        LOG.debug("GraphMLSearchProvider->removeVertexHopCriteria: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    private Optional<GraphML> getGraph() {
        return Optional.ofNullable(GraphMLTopologyProvider.lastGraph);
    }
}
