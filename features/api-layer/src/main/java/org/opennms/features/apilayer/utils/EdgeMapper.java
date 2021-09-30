/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.utils;

import java.util.Objects;

import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.integration.api.v1.model.TopologyPort;
import org.opennms.integration.api.v1.model.TopologySegment;
import org.opennms.integration.api.v1.model.immutables.ImmutableNode;
import org.opennms.integration.api.v1.model.immutables.ImmutableNodeCriteria;
import org.opennms.integration.api.v1.model.immutables.ImmutableTopologyEdge;
import org.opennms.integration.api.v1.model.immutables.ImmutableTopologyPort;
import org.opennms.integration.api.v1.model.immutables.ImmutableTopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

public class EdgeMapper {
    private final NodeCriteriaCache nodeCriteriaCache;

    public EdgeMapper(NodeCriteriaCache nodeCriteriaCache) {
        this.nodeCriteriaCache = Objects.requireNonNull(nodeCriteriaCache);
    }

    public TopologyEdge toEdge(OnmsTopologyProtocol protocol, OnmsTopologyEdge edge) {
        ImmutableTopologyEdge.Builder topologyEdge = ImmutableTopologyEdge.newBuilder()
                .setId(edge.getId())
                .setProtocol(ModelMappers.toTopologyProtocol(protocol))
                .setTooltipText(edge.getToolTipText());

        // Set the source
        if (edge.getSource().getVertex().getNodeid() == null) {
            // Source is a segment
            topologyEdge.setSource(getSegment(edge.getSource(), protocol));
        } else if (edge.getSource().getIfindex() != null && edge.getSource().getIfindex() >= 0) {
            // Source is a port
            topologyEdge.setSource(getPort(edge.getSource()));
        } else {
            // Source is a node
            topologyEdge.setSource(getNode(edge.getSource()));
        }

        // Set the target
        if (edge.getTarget().getVertex().getNodeid() == null) {
            // Target is a segment
            topologyEdge.setTarget(getSegment(edge.getTarget(), protocol));
        } else if (edge.getTarget().getIfindex() != null && edge.getTarget().getIfindex() >= 0) {
            // Target is a port
            topologyEdge.setTarget(getPort(edge.getTarget()));
        } else {
            // Target is a node
            topologyEdge.setTarget(getNode(edge.getTarget()));
        }

        return topologyEdge.build();
    }

    private TopologySegment getSegment(OnmsTopologyPort port, OnmsTopologyProtocol protocol) {
        return ImmutableTopologySegment.newBuilder()
                .setId(port.getId())
                .setTooltipText(port.getToolTipText())
                .setProtocol(ModelMappers.toTopologyProtocol(protocol))
                .build();
    }

    private TopologyPort getPort(OnmsTopologyPort port) {
        ImmutableTopologyPort.Builder portBuilder = ImmutableTopologyPort.newBuilder()
                .setId(port.getId())
                .setTooltipText(port.getToolTipText())
                .setIfIndex(port.getIfindex())
                .setIfName(port.getIfname())
                .setIfAddress(port.getAddr());

        Integer nodeId = port.getVertex().getNodeid();
        ImmutableNodeCriteria.Builder nodeCriteriaBuilder = ImmutableNodeCriteria.newBuilder().setId(nodeId);

        nodeCriteriaCache.getNodeCriteria(nodeId.longValue()).ifPresent(nc -> {
            nodeCriteriaBuilder.setForeignSource(nc.getForeignSource());
            nodeCriteriaBuilder.setForeignId(nc.getForeignId());
        });

        portBuilder.setNodeCriteria(nodeCriteriaBuilder.build());

        return portBuilder.build();
    }

    private Node getNode(OnmsTopologyPort port) {
        Integer nodeId = port.getVertex().getNodeid();
        ImmutableNode.Builder nodeBuilder = ImmutableNode.newBuilder()
                .setId(nodeId);

        nodeCriteriaCache.getNodeCriteria(nodeId.longValue()).ifPresent(nc -> {
            nodeBuilder.setForeignSource(nc.getForeignSource());
            nodeBuilder.setForeignId(nc.getForeignId());
        });

        return nodeBuilder.build();
    }
}
