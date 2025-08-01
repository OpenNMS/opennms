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
package org.opennms.netmgt.graph.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.NodeService;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.opennms.netmgt.graph.api.info.StatusInfo;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultNodeService implements NodeService {

    private final GenericPersistenceAccessor accessor;
    private final AlarmDao alarmDao;

    public DefaultNodeService(final GenericPersistenceAccessor accessor, final AlarmDao alarmDao) {
        this.accessor = Objects.requireNonNull(accessor);
        this.alarmDao = Objects.requireNonNull(alarmDao);
    }

    @Override
    public List<NodeInfo> resolveNodes(List<NodeRef> nodeRefs) {
        Objects.requireNonNull(nodeRefs);
        final List<OnmsNode> nodes = loadNodes(nodeRefs);
        final List<NodeInfo> nodeInfoList = nodes.stream().map(node -> {
            final Set<String> categories = node.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toSet());
            final List<IpInfo> ipInfoList = node.getIpInterfaces().stream().map(ifc -> new IpInfo(ifc.getIpAddress(), ifc.isPrimary(), ifc.isManaged())).collect(Collectors.toList());
            return new NodeInfo.NodeInfoBuilder().
                    id(node.getId())
                    .label(node.getLabel())
                    .foreignId(node.getForeignId())
                    .foreignSource(node.getForeignSource())
                    .location(node.getLocation().getLocationName())
                    .categories(categories)
                    .ipInterfaces(ipInfoList)
                    .build();
        }).collect(Collectors.toList());
        return nodeInfoList;
    }

    @Override
    public Map<NodeRef, StatusInfo> resolveStatus(List<NodeRef> nodeRefs) {
        final Map<Integer, NodeRef> nodeIdNodeRefMap = new HashMap<>();
        nodeRefs.stream()
                .filter(nodeRef -> nodeRef.getNodeId() != null)
                .forEach(nr -> nodeIdNodeRefMap.put(nr.getNodeId(), nr));

        final List<NodeRef> foreignIdNodeRefs = nodeRefs.stream()
                .filter(nodeRef -> nodeRef.getNodeId() == null)
                .collect(Collectors.toList());
        final List<OnmsNode> nodes = loadNodes(foreignIdNodeRefs);
        for (OnmsNode eachNode : nodes) {
            for (NodeRef eachRef : nodeRefs) {
                if (eachRef.matches(eachNode)) {
                    nodeIdNodeRefMap.put(eachNode.getId(), eachRef);
                }
            }
        }
        // Alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = getAlarmSummaries(nodeIdNodeRefMap.keySet());

        // Set the result
        final Map<NodeRef, StatusInfo> resultMap = Maps.newHashMap();
        for (Integer nodeId : nodeIdNodeRefMap.keySet()) {
            final AlarmSummary alarmSummary = nodeIdToAlarmSummaryMap.get(nodeId);
            final StatusInfo status = alarmSummary == null
                    ? StatusInfo.defaultStatus().build()
                    : StatusInfo.builder(alarmSummary.getMaxSeverity()).count(alarmSummary.getAlarmCount()).build();
            final NodeRef nodeRef = nodeIdNodeRefMap.get(nodeId);
            resultMap.put(nodeRef, status);
        }
        return resultMap;
    }

    private Map<Integer, AlarmSummary> getAlarmSummaries(Set<Integer> nodeIds) {
        return alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(Lists.newArrayList(nodeIds))
                .stream()
                .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity()));
    }

    private List<OnmsNode> loadNodes(final List<NodeRef> nodeRefs) {
        final List<Integer> nodeIds = nodeRefs.stream().map(n -> n.getNodeId()).filter(id -> id != null).collect(Collectors.toList());
        final List<String> foreignSources = nodeRefs.stream().map(n -> n.getForeignSource()).filter(fs -> fs != null).distinct().collect(Collectors.toList());
        final List<String> foreignIds = nodeRefs.stream().map(n -> n.getForeignId()).filter(fid -> fid != null).distinct().collect(Collectors.toList());
        final List<OnmsNode> nodes = new NodeQuery().execute(nodeIds, foreignSources, foreignIds);
        return nodes;
    }

    private final class NodeQuery {
        public List<OnmsNode> execute(List<Integer> nodeIds, List<String> foreignSources, List<String> foreignIds) {
            Objects.requireNonNull(nodeIds);
            Objects.requireNonNull(foreignSources);
            Objects.requireNonNull(foreignIds);

            // no data provided => empty list
            if (nodeIds.isEmpty() && foreignSources.isEmpty() && foreignIds.isEmpty()) {
                return Lists.newArrayList();
            }

            // only nodeIds are defined
            if (!nodeIds.isEmpty() && foreignSources.isEmpty() && foreignIds.isEmpty()) {
                return accessor.findUsingNamedParameters("select n from OnmsNode n where n.id in (:nodeIds)",
                        new String[] { "nodeIds" },
                        new Object[] { nodeIds });
            }

            // Only foreignSources AND foreignIds are defined
            if (nodeIds.isEmpty() && !foreignSources.isEmpty() && !foreignIds.isEmpty()) {
                return accessor.findUsingNamedParameters("select n from OnmsNode n where n.foreignSource in (:foreignSources) and n.foreignId in (:foreignIds)",
                        new String[] { "foreignSources", "foreignIds" },
                        new Object[] { foreignSources, foreignIds }
                );
            }

            // Everything is defined
            return accessor.findUsingNamedParameters("select n from OnmsNode n where n.id in (:nodeIds) or (n.foreignSource in (:foreignSources) and n.foreignId in (:foreignIds))",
                    new String[] { "nodeIds", "foreignSources", "foreignIds" },
                    new Object[] {  nodeIds, foreignSources, foreignIds}
            );
        }
    }
}
