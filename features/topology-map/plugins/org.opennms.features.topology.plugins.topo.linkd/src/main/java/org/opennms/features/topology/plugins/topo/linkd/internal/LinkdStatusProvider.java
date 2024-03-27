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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.CollapsibleGraph;
import org.opennms.features.topology.api.topo.CollapsibleRef;
import org.opennms.features.topology.api.topo.CollapsibleVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.slf4j.LoggerFactory;

public class LinkdStatusProvider implements StatusProvider {

    private static class AlarmStatus extends DefaultStatus {
        public AlarmStatus(String label, long count) {
            super(label, count);
        }
    }

    @Override
    public String getNamespace() {
        return m_linkdTopologyFactory.getActiveNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && getNamespace().equals(namespace);
    }

    private final AlarmDao m_alarmDao;
    private final LinkdTopologyFactory m_linkdTopologyFactory;
    public LinkdStatusProvider(AlarmDao alarmDao, LinkdTopologyFactory linkdTopologyFactory) {
        m_alarmDao = alarmDao;
        m_linkdTopologyFactory=linkdTopologyFactory;
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
        Map<VertexRef, Status> returnMap = new HashMap<>();

        // split nodes from groups and others
        List<VertexRef> nodeRefs = getNodeVertexRefs(graph, vertices, criteria); // nodes
        List<VertexRef> otherRefs = getOtherVertexRefs(graph, vertices);  // groups

        Map<Integer, VertexRef> nodeIdMap = extractNodeIds(nodeRefs);
        Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIdMap.keySet()); // calculate status for ALL nodes

        // status for all known node ids
        for (Integer eachNodeId : nodeIdMap.keySet()) {
            AlarmSummary summary = nodeIdToAlarmSummaryMap.get(eachNodeId);
            AlarmStatus status = summary == null ? createDefaultStatus() : createStatus(summary);
            VertexRef ref = nodeIdMap.get(eachNodeId);
            returnMap.put(ref, status);

            LoggerFactory.getLogger(getClass()).debug("Status for node '{}' with id '{}' is: {}", ref.getLabel(), ref.getId(), status);
        }

        // calculate status for groups and nodes which are neither group nor node
        for (VertexRef eachRef : otherRefs) {
            if (isCollapsible(eachRef)) {
                List<AlarmSummary> alarmSummariesForGroup = new ArrayList<>();
                List<Vertex> children = new CollapsibleGraph(graph).getVertices((CollapsibleRef) eachRef, criteria);
                for (Vertex eachChildren : children) {
                    AlarmSummary eachChildrenAlarmSummary = nodeIdToAlarmSummaryMap.get(eachChildren.getNodeID());
                    if (eachChildrenAlarmSummary != null) {
                        alarmSummariesForGroup.add(eachChildrenAlarmSummary);
                    }
                }
                AlarmStatus groupStatus = calculateAlarmStatusForGroup(alarmSummariesForGroup);
                returnMap.put(eachRef, groupStatus);
            } else {
                returnMap.put(eachRef, createDefaultStatus());
            }
        }

        return returnMap;
    }

    private Map<Integer , AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        Map<Integer, AlarmSummary> resultMap = new HashMap<>();

        List<AlarmSummary> alarmSummaries = m_alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(new ArrayList<>(nodeIds));
        for (AlarmSummary eachSummary : alarmSummaries) {
            resultMap.put(eachSummary.getNodeId(), eachSummary);
        }

        return resultMap;
    }

    private static AlarmStatus createStatus(AlarmSummary summary) {
        return new AlarmStatus(summary.getMaxSeverity().getLabel(), summary.getAlarmCount());
    }

    private static Map<Integer, VertexRef> extractNodeIds(Collection<VertexRef> inputList) {
        Map<Integer, VertexRef> vertexRefToNodeIdMap = new HashMap<>();

        for (VertexRef eachRef : inputList) {
            try {
                Integer nodeId = Integer.parseInt(eachRef.getId());
                vertexRefToNodeIdMap.put(nodeId, eachRef);
            } catch (NumberFormatException nfe) {
                LoggerFactory.getLogger(LinkdStatusProvider.class).warn("Could not parse id '{}' of vertex '{}' as integer.", eachRef.getId(), eachRef);
            }
        }

        return vertexRefToNodeIdMap;
    }

    private static List<VertexRef> getNodeVertexRefs(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
        List<VertexRef> returnList = new ArrayList<>();
        for (VertexRef eachRef : vertices) {
            if (graph.getNamespace().equals(eachRef.getNamespace())) {
                if(isCollapsible(eachRef)) {
                    addChildrenRecursively(graph, (CollapsibleRef) eachRef, returnList, criteria);
                } else {
                    if (!returnList.contains(eachRef)) {
                        returnList.add(eachRef);
                    }
                }
            }
        }
        return returnList;
    }

    private static List<VertexRef> getOtherVertexRefs(BackendGraph graph, Collection<VertexRef> vertices) {
        List<VertexRef> returnList = new ArrayList<>();
        for (VertexRef eachRef : vertices) {
            if (!graph.getNamespace().equals(eachRef.getNamespace())) {
                returnList.add(eachRef); // we do not need to check for groups, because a group would have a namespace "nodes"
            }
        }
        return returnList;
    }

    private static void addChildrenRecursively(BackendGraph graph, CollapsibleRef collapsibleRef, Collection<VertexRef> vertexRefs, Criteria[] criteria) {
        List<Vertex> vertices = new CollapsibleGraph(graph).getVertices(collapsibleRef, criteria);
        for(Vertex vertex : vertices) {
            if(!isCollapsible(vertex)) {
                if (!vertexRefs.contains(vertex)) {
                    vertexRefs.add(vertex);
                }
            } else {
                addChildrenRecursively(graph, collapsibleRef, vertexRefs, criteria);
            }
        }
    }

    private static boolean isCollapsible(VertexRef vertexRef) {
        return vertexRef instanceof CollapsibleVertex;
    }

    private static AlarmStatus createDefaultStatus() {
        return new AlarmStatus(OnmsSeverity.NORMAL.getLabel(), 0);
    }

    private static AlarmStatus calculateAlarmStatusForGroup(List<AlarmSummary> alarmSummaries) {
        if (!alarmSummaries.isEmpty()) {
            alarmSummaries.sort(Comparator.comparing(AlarmSummary::getMaxSeverity));
            OnmsSeverity severity = alarmSummaries.get(0).getMaxSeverity();
            int count = 0;
            for (AlarmSummary eachSummary : alarmSummaries) {
                count += eachSummary.getAlarmCount();
            }
            return new AlarmStatus(severity.getLabel(), count);
        }
        return createDefaultStatus();
    }
}
