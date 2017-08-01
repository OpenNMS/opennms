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

package org.opennms.features.topology.plugins.topo.graphml.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertex;
import org.opennms.features.topology.plugins.topo.graphml.internal.AlarmSummaryWrapper;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Maps;

public class GraphMLDefaultVertexStatusProvider implements StatusProvider {

    private final String namespace;
    private final AlarmSummaryWrapper alarmSummaryWrapper;

    public GraphMLDefaultVertexStatusProvider(final String namespace,
                                              final AlarmSummaryWrapper alarmSummaryWrapper) {
        this.namespace = Objects.requireNonNull(namespace);
        this.alarmSummaryWrapper = Objects.requireNonNull(alarmSummaryWrapper);
    }

    @Override
    public boolean contributesTo(String namespace) {
        return this.getNamespace().equals(namespace);
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(VertexProvider vertexProvider, Collection<VertexRef> vertices, Criteria[] criteria) {
        // All vertices for the current vertexProvider
        final List<GraphMLVertex> graphMLVertices = vertices.stream()
                                                            .filter(eachVertex -> contributesTo(eachVertex.getNamespace()) && eachVertex instanceof GraphMLVertex)
                                                            .map(eachVertex -> (GraphMLVertex) eachVertex)
                                                            .collect(Collectors.toList());

        // All vertices associated with a node id
        final Map<Integer, VertexRef> nodeIdMap = graphMLVertices.stream()
                .filter(eachVertex -> eachVertex.getNodeID() != null)
                .collect(Collectors.toMap(AbstractVertex::getNodeID, Function.identity()));

        // Alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIdMap.keySet());

        // Set the result
        Map<VertexRef, Status> resultMap = Maps.newHashMap();
        for (GraphMLVertex eachVertex : graphMLVertices) {
            AlarmSummary alarmSummary = nodeIdToAlarmSummaryMap.get(eachVertex.getNodeID());
            GraphMLVertexStatus status = alarmSummary == null
                                   ? new GraphMLVertexStatus()
                                   : new GraphMLVertexStatus(alarmSummary.getMaxSeverity(), alarmSummary.getAlarmCount());
            resultMap.put(eachVertex, status);
        }

        return resultMap;
    }

    private Map<Integer, AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        return alarmSummaryWrapper.getAlarmSummaries(new ArrayList<>(nodeIds))
                .stream()
                .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity()));
    }
}
