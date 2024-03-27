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
package org.opennms.features.topology.plugins.topo.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.provider.application.ApplicationGraph;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
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

        // we combine the suggest and search phase since the old search doesn't distinguish between suggesting and searching.
        // This is some ugly mapping here to marry the old and new world...

        List<SearchSuggestion> suggestions = graphSearchService.getSuggestions(ApplicationGraph.NAMESPACE, searchQuery.getQueryString());
        List<GenericVertex> suggestedVertices = new ArrayList<>();

        for (SearchSuggestion suggestion : suggestions) {
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setNamespace(ApplicationGraph.NAMESPACE);
            // context shouldn't matter: searchCriteria.setContext();
            searchCriteria.setCriteria(suggestion.getId());
            searchCriteria.setProviderId(suggestion.getProvider());
            suggestedVertices.addAll(graphSearchService.search(searchCriteria));
        }

        List<SearchResult> results = Lists.newArrayList();
        for (GenericVertex genericVertex : suggestedVertices) {
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

