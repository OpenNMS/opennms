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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LegacyStatusProvider implements StatusProvider, EdgeStatusProvider {

    private final String namespace;
    private final AlarmDao alarmDao;

    public LegacyStatusProvider(final String namespace, final AlarmDao alarmDao) {
        this.namespace = Objects.requireNonNull(namespace);
        this.alarmDao = Objects.requireNonNull(alarmDao);
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
        // All vertices for the current vertexProvider
        final List<LegacyVertex> legacyVertices = vertices.stream()
                .filter(eachVertex -> contributesTo(eachVertex.getNamespace()) && eachVertex instanceof LegacyVertex)
                .map(eachVertex -> (LegacyVertex) eachVertex)
                .collect(Collectors.toList());

        // All vertices associated with a node id
        final Map<Integer, VertexRef> nodeIdMap = legacyVertices.stream()
                .filter(eachVertex -> eachVertex.getNodeID() != null)
                .collect(Collectors.toMap(AbstractVertex::getNodeID, Function.identity()));

        // Alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIdMap.keySet());

        // Set the result
        Map<VertexRef, Status> resultMap = Maps.newHashMap();
        for (LegacyVertex eachVertex : legacyVertices) {
            final AlarmSummary alarmSummary = nodeIdToAlarmSummaryMap.get(eachVertex.getNodeID());
            final DefaultStatus status = alarmSummary == null
                    ? new DefaultStatus(OnmsSeverity.NORMAL.getLabel(), 0)
                    : new DefaultStatus(alarmSummary.getMaxSeverity().getLabel(), alarmSummary.getAlarmCount());
            resultMap.put(eachVertex, status);
        }

        return resultMap;
    }

    @Override
    public Map<? extends EdgeRef, ? extends Status> getStatusForEdges(BackendGraph graph, Collection<EdgeRef> edges, Criteria[] criteria) {
        final HashMap<EdgeRef, Status> edgeStatusMap = Maps.newHashMap();
        for (EdgeRef eachEdge : edges) {
            edgeStatusMap.put(eachEdge, new Status() {

                @Override
                public String computeStatus() {
                    return "up";
                }

                @Override
                public Map<String, String> getStatusProperties() {
                    return ImmutableMap.of("status", "up");
                }

                @Override
                public Map<String, String> getStyleProperties() {
                    return Maps.newHashMap();
                }
            });
        }
        return edgeStatusMap;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }


    private Map<Integer, AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        return alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(Lists.newArrayList(nodeIds))
                .stream()
                .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity()));
    }
}
