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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph;

public class LegacyBackendGraph extends SimpleGraph {
    private final GenericGraph delegate;

    public LegacyBackendGraph(GenericGraph genericGraph) {
        super(Objects.requireNonNull(genericGraph.getNamespace()));
        genericGraph.getVertices().forEach(genericVertex -> addVertices(new LegacyVertex(genericVertex)));
        genericGraph.getEdges().forEach(genericEdge -> addEdges(new LegacyEdge(genericEdge)));
        this.delegate = genericGraph;
    }

    public List<Criteria> getDefaultCriteria() {
        return delegate.getDefaultFocus().getVertexIds().stream()
                .map(vertexId -> new DefaultVertexHopCriteria(new DefaultVertexRef(getNamespace(), vertexId)))
                .collect(Collectors.toList());
    }

    @Override
    public LegacyVertex getVertex(String namespace, String id) {
        return (LegacyVertex) super.getVertex(namespace, id);
    }
}
