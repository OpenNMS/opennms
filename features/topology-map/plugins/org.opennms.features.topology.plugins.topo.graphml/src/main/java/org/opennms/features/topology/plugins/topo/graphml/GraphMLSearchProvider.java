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
package org.opennms.features.topology.plugins.topo.graphml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.simple.SimpleSearchProvider;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * {@link org.opennms.features.topology.api.topo.SearchProvider} for GraphML definitions.
 * The provider searches for matching vertices in ALL graphs, not only the current visible one.
 */
public class GraphMLSearchProvider extends SimpleSearchProvider {

    private final GraphMLTopologyProvider graphMLTopologyProvider;

    public GraphMLSearchProvider(GraphMLTopologyProvider graphMLTopologyProvider) {
        this.graphMLTopologyProvider = Objects.requireNonNull(graphMLTopologyProvider);
    }

    /**
     * In GraphML graphs the namespace of each graph contained in the GraphML file should be prefixed, e.g.
     * namespace1:graph1, namespace1:graph2, etc.
     *
     * @param namespace The namespace to check
     * @return true if this {@link org.opennms.features.topology.api.topo.SearchProvider} contributes, false otherwise
     */
    @Override
    public boolean contributesTo(String namespace) {
        boolean contributes = super.contributesTo(namespace);
        if (!contributes && namespace.contains(":")) {
            String prefix = namespace.substring(0, namespace.indexOf(":"));
            return getSearchProviderNamespace().startsWith(prefix);
        }
        return contributes;
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        final GraphContainer graphContainer = operationContext.getGraphContainer();
        final DefaultVertexRef vertexRef = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        if (graphContainer.getTopologyServiceClient().getVertex(vertexRef) == null) {
            // The vertex to add to focus is not in the current layer
            // Find the GraphProvider it belongs to
            Optional<GraphProvider> first = graphContainer.getTopologyServiceClient().getGraphProviders().stream()
                    .filter(eachProvider -> eachProvider.getNamespace().equals(searchResult.getNamespace()))
                    .findFirst();
            // If there is a graph provider (which should) select it
            if (first.isPresent() && first.get().getCurrentGraph().getVertex(vertexRef) != null) {
                graphContainer.selectTopologyProvider(first.get());
                graphContainer.clearCriteria();
            }
        }
        super.onFocusSearchResult(searchResult, operationContext);
    }

    @Override
    public String getSearchProviderNamespace() {
        return graphMLTopologyProvider.getNamespace();
    }

    @Override
    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        final List<GraphMLVertex> matchingVertices = new ArrayList<>();
        graphMLTopologyProvider.getCurrentGraph().getVertices().stream()
            .map(v -> (GraphMLVertex) v)
            .filter(v -> matches(searchQuery, v))
            .sorted((v1, v2) -> v1.getId().compareTo(v2.getId()))
            .forEach(matchingVertices::add);
        return matchingVertices;
    }

    /**
     * Returns true if either if either the graph node's id, or the values of any
     * of the graph node's properties contain the query string.
     */
    private static boolean matches(SearchQuery searchQuery, GraphMLVertex graphMLVertex) {
        final String qs = searchQuery.getQueryString().toLowerCase();
        for (Object propValue : graphMLVertex.getProperties().values()) {
            final String value = propValue != null ? propValue.toString() : "";
            if (value.toLowerCase().contains(qs)) {
                return true;
            }
        }
        return graphMLVertex.getId().toLowerCase().contains(qs);
    }
}
