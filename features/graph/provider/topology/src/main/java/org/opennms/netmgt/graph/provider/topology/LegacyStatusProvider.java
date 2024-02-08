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
