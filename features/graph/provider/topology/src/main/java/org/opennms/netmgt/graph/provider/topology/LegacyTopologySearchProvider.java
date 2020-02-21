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
