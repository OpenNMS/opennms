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
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
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
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
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
