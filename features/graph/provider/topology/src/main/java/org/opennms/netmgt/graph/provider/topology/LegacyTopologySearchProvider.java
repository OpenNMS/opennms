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
package org.opennms.netmgt.graph.provider.topology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.AbstractRef;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleSearchProvider;

public class LegacyTopologySearchProvider extends SimpleSearchProvider {

    private final LegacyTopologyProvider delegate;

    public LegacyTopologySearchProvider(LegacyTopologyProvider legacyTopologyProvider) {
        this.delegate = Objects.requireNonNull(legacyTopologyProvider);
    }

    @Override
    public String getSearchProviderNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        final List<LegacyVertex> matchingVertices = new ArrayList<>();
        delegate.getCurrentGraph().getVertices().stream()
                .map(v -> (LegacyVertex) v)
                .filter(v -> matches(searchQuery, v))
                .sorted(Comparator.comparing(AbstractRef::getId))
                .forEach(matchingVertices::add);
        return matchingVertices;
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
