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

package org.opennms.features.topology.plugins.topo.graphml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class GraphMLEdgeStatusProvider implements EdgeStatusProvider {

    private static class GraphMLEdgeStatus implements Status {

        private Map<String, String> styleProperties = Maps.newHashMap();

        @Override
        public String computeStatus() {
            return null;
        }

        @Override
        public Map<String, String> getStatusProperties() {
            return Maps.newHashMap();
        }

        @Override
        public Map<String, String> getStyleProperties() {
            return styleProperties;
        }

        public GraphMLEdgeStatus withStyle(String key, String value) {
            styleProperties.put(key, value);
            return this;
        }
    }

    private GraphMLTopologyProvider provider;

    public GraphMLEdgeStatusProvider(GraphMLTopologyProvider provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        final List<GraphMLEdge> collectedList = edges.stream()
                .filter(eachEdge -> eachEdge instanceof GraphMLEdge)
                .map(eachEdge -> (GraphMLEdge) eachEdge)
                .collect(Collectors.toList());
        final ArrayList<String> colors = Lists.newArrayList("blue", "yellow", "green", "purple", "red");
        final Map<EdgeRef, Status> resultMap = Maps.newHashMap();
        int colorIndex = 0;
        for (GraphMLEdge eachEdge : collectedList) {
            if (colorIndex == colors.size() - 1) {
                colorIndex = 0;
            }
            Status status = new GraphMLEdgeStatus().withStyle("stroke", colors.get(colorIndex));
            resultMap.put(eachEdge, status);
            colorIndex++;
        }
        return resultMap;
    }

    @Override
    public String getNamespace() {
        return provider.getVertexNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }
}
