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

package org.opennms.features.topology.api.topo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class SimpleSearchProvider extends AbstractSearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchProvider.class);

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

    public abstract List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container);
    
    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer container) {
        LOG.info("SimpleSearchProvider->query: called with search query: '{}'", searchQuery);

        // Build search results from the matching vertices
        final List<SearchResult> results = queryVertices(searchQuery, container).stream().map(v -> {
            SearchResult searchResult = new SearchResult(v, true, false);
            return searchResult;
        }).collect(Collectors.toList());

        LOG.info("SimpleSearchProvider->query: found {} results: {}", results.size(), results);
        return results;
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        final VertexRef vertexToFocus = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        return Sets.newHashSet(vertexToFocus);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SimpleSearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        final DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.addCriteria(criterion);
        LOG.debug("SimpleSearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        LOG.debug("SimpleSearchProvider->addVertexHop: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SimpleSearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        final DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(
                new DefaultVertexRef(
                        searchResult.getNamespace(),
                        searchResult.getId(),
                        searchResult.getLabel()));
        container.removeCriteria(criterion);

        LOG.debug("SimpleSearchProvider->removeVertexHopCriteria: current criteria {}.", Arrays.toString(container.getCriteria()));
    }
}
