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
package org.opennms.netmgt.graph.search;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchProvider;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelSearchProvider implements SearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LabelSearchProvider.class);

    @Override
    public boolean canSuggest(GraphService graphService, String namespace) {
        return true; // every vertex has a label, therefore we can search any graph
    }

    @Override
    public List<SearchSuggestion> getSuggestions(SearchContext searchContext, String namespace, String input) {
        Objects.requireNonNull(input);
        return getVerticesOfGraph(searchContext.getGraphService(), namespace)
                .stream()
                .filter(v -> v.getLabel() != null && v.getLabel().toLowerCase().contains(input.toLowerCase()))
                .map(v -> new SearchSuggestion(getProviderId(), GenericVertex.class.getSimpleName(), v.getLabel(), v.getLabel()))
                .limit(searchContext.getSuggestionsLimit())
                .collect(Collectors.toList());
    }

    @Override
    public List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria) {
        final List<GenericVertex> vertices = getVerticesOfGraph(graphService, searchCriteria.getNamespace())
                .stream()
                .filter(v -> v.getLabel() != null && v.getLabel().toLowerCase().contains(searchCriteria.getCriteria().toLowerCase()))
                .collect(Collectors.toList());
        return vertices;
    }

    private List<GenericVertex> getVerticesOfGraph(GraphService graphService, String namespace) {
        GenericGraph graph = graphService.getGraph(namespace);
        List<GenericVertex> result;
        if (graph != null) {
            result = graph.getVertices();
        } else {
            LOG.warn("Could not find graph for namespace {}", namespace);
            result = Collections.emptyList();
        }
        return result;
    }
}
