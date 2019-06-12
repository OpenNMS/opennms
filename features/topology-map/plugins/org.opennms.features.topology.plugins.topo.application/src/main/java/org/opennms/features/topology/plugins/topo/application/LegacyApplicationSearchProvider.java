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

package org.opennms.features.topology.plugins.topo.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.provider.application.ApplicationGraph;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * We call here the new search implementation and use it in the legacy world. This class will disappear eventually and the
 * new search will be used directly.
 */

public class LegacyApplicationSearchProvider extends AbstractSearchProvider implements SearchProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyApplicationSearchProvider.class);

    private GraphSearchService graphSearchService;
    
    @Override
    public String getSearchProviderNamespace() {
        return LegacyApplicationTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return LegacyApplicationTopologyProvider.TOPOLOGY_NAMESPACE.equalsIgnoreCase(namespace);
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix("application=", searchPrefix);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer container) {
        LOG.info("ApplicationServiceSearchProvider->query: called with search query: '{}'", searchQuery);

        // we skip the suggest phase since the old search doesn't distinguish between suggesting and searching.

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setNamespace(ApplicationGraph.TOPOLOGY_NAMESPACE);
        // context shouldn't matter: searchCriteria.setContext();
        searchCriteria.setCriteria(searchQuery.getQueryString());
        List<GenericVertex> vertices = graphSearchService.search(searchCriteria);

        List<SearchResult> results = Lists.newArrayList();

        for (GenericVertex genericVertex : vertices) {
            ApplicationVertex applicationVertex = new ApplicationVertex(genericVertex);
            final LegacyApplicationVertex legacyApplicationVertex = new LegacyApplicationVertex(applicationVertex);
            SearchResult searchResult = new SearchResult(legacyApplicationVertex, true, false);
            results.add(searchResult);
        }

        LOG.info("ApplicationServiceSearchProvider->query: found {} results: {}", results.size(), results);
        return results;
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        VertexRef vertexToFocus = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        return Sets.newHashSet(vertexToFocus);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("ApplicationServiceSearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.addCriteria(criterion);

        LOG.debug("ApplicationServiceSearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        LOG.debug("ApplicationServiceSearchProvider->addVertexHop: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("ApplicationServiceSearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.removeCriteria(criterion);

        LOG.debug("ApplicationServiceSearchProvider->removeVertexHopCriteria: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    public void setGraphSearchService(GraphSearchService graphSearchService) {
        this.graphSearchService = graphSearchService;
    }
}

