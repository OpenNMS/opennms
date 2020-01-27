/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.topology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractRef;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Sets;

// TODO MVR more or less a copy of simplesearchprovider
public class LegacyTopologySearchProvider extends AbstractSearchProvider {

    private final LegacyTopologyProvider delegate;

    public LegacyTopologySearchProvider(LegacyTopologyProvider legacyTopologyProvider) {
        this.delegate = Objects.requireNonNull(legacyTopologyProvider);
    }

    @Override
    public String getSearchProviderNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getSearchProviderNamespace().equals(namespace);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
        // Build search results from the matching vertices
        final List<SearchResult> results = queryVertices(searchQuery, graphContainer).stream().map(v -> {
            SearchResult searchResult = new SearchResult(v, true, false);
            return searchResult;
        }).collect(Collectors.toList());
        return results;
    }

    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        final List<LegacyVertex> matchingVertices = new ArrayList<>();
        delegate.getCurrentGraph().getVertices().stream()
                .map(v -> (LegacyVertex) v)
                .filter(v -> matches(searchQuery, v))
                .sorted(Comparator.comparing(AbstractRef::getId))
                .forEach(matchingVertices::add);
        return matchingVertices;
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        final String graphNamespace = getSearchProviderNamespace();
        final String graphPrefix = graphNamespace != null ? graphNamespace : "";
        return AbstractSearchProvider.supportsPrefix(graphPrefix + "=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer graphContainer) {
        final VertexRef vertexToFocus = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        return Sets.newHashSet(vertexToFocus);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {

        final DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.addCriteria(criterion);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        final DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.removeCriteria(criterion);
    }

    /**
     * Returns true if either if either the graph node's id, or the values of any
     * of the graph node's properties contain the query string.
     */
    private static boolean matches(SearchQuery searchQuery, LegacyVertex legacyVertex) {
        final String qs = searchQuery.getQueryString().toLowerCase();
        for (Object propValue : legacyVertex.getProperties().values()) {
            final String value = propValue != null ? propValue.toString() : "";
            if (value.toLowerCase().contains(qs)) {
                return true;
            }
        }
        return legacyVertex.getId().toLowerCase().contains(qs);
    }
}
