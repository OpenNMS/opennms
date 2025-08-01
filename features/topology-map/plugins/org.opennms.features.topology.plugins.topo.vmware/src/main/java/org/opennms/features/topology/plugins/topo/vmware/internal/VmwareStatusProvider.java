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
package org.opennms.features.topology.plugins.topo.vmware.internal;

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
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Maps;

public class VmwareStatusProvider implements StatusProvider {

    private final AlarmDao alarmDao;

    public VmwareStatusProvider(AlarmDao alarmDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
        final List<AbstractVertex> vmwareVertices = vertices.stream()
                .filter(v -> v.getNamespace().equals(getNamespace()))
                .map(v -> (AbstractVertex) v)
                .collect(Collectors.toList());

        // All vertices associated with a node id
        final Map<Integer, VertexRef> nodeIdMap = vmwareVertices.stream()
                .filter(v -> v.getNodeID() != null)
                .collect(Collectors.toMap(AbstractVertex::getNodeID, Function.identity()));

        // Alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIdMap.keySet());

        // Set the result
        Map<VertexRef, Status> resultMap = Maps.newHashMap();
        for (AbstractVertex eachVertex : vmwareVertices) {
            AlarmSummary alarmSummary = nodeIdToAlarmSummaryMap.get(eachVertex.getNodeID());
            if (alarmSummary != null) {
                resultMap.put(eachVertex, new DefaultStatus(alarmSummary.getMaxSeverity().getLabel(), 0));
            }
        }
        return resultMap;
    }

    @Override
    public String getNamespace() {
        return VmwareTopologyProvider.TOPOLOGY_NAMESPACE_VMWARE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }

    private Map<Integer, AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        return alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(new ArrayList<>(nodeIds))
                .stream()
                .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity()));
    }
}
