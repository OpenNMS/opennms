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
package org.opennms.features.topology.api.topo.simple;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
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
