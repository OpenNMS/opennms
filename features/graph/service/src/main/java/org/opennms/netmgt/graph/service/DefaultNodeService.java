/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.NodeService;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class DefaultNodeService implements NodeService {

    private final GenericPersistenceAccessor accessor;

    public DefaultNodeService(final GenericPersistenceAccessor accessor) {
        this.accessor = Objects.requireNonNull(accessor);
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
