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

package org.opennms.topologies.service.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class EdgeMockUtil {
    public static final String PROTOCOL = "CDP";
    public static final String EDGE_ID = "edge.id";
    public static final String EDGE_TOOLTIP = "edge.tooltip";
    public static final String SOURCE_TOOLTIP = "edge.source.tooltip";
    public static final String TARGET_TOOLTIP = "edge.target.tooltip";
    public static final String SOURCE_ID = "source.id";
    public static final String TARGET_ID = "target.id";
    public static final int SOURCE_NODE_ID = 1;
    public static final int TARGET_NODE_ID = 2;
    public static final int SOURCE_IFINDEX = 101;
    public static final int TARGET_IFINDEX = 102;
    public static final String SOURCE_VERTEX_ID = "source.vertex.id";
    public static final String TARGET_VERTEX_ID = "target.vertex.id";

    public static OnmsTopologyEdge createEdge() {
        OnmsTopologyEdge edgeMock = mock(OnmsTopologyEdge.class);
        when(edgeMock.getId()).thenReturn(EDGE_ID);
        when(edgeMock.getToolTipText()).thenReturn(EDGE_TOOLTIP);
        return edgeMock;
    }

    public static void addPort(boolean isSource, OnmsTopologyEdge edge) {
        OnmsTopologyPort portMock = mock(OnmsTopologyPort.class);

        OnmsTopologyVertex vertexPortMock = mock(OnmsTopologyVertex.class);
        when(portMock.getVertex()).thenReturn(vertexPortMock);

        if (isSource) {
            when(edge.getSource()).thenReturn(portMock);
            when(portMock.getIfindex()).thenReturn(SOURCE_IFINDEX);
            when(portMock.getId()).thenReturn(SOURCE_ID);
            when(vertexPortMock.getNodeid()).thenReturn(SOURCE_NODE_ID);
            when(portMock.getToolTipText()).thenReturn(SOURCE_TOOLTIP);
            when(vertexPortMock.getId()).thenReturn(SOURCE_VERTEX_ID);
        } else {
            when(edge.getTarget()).thenReturn(portMock);
            when(portMock.getId()).thenReturn(TARGET_ID);
            when(vertexPortMock.getNodeid()).thenReturn(TARGET_NODE_ID);
            when(portMock.getIfindex()).thenReturn(TARGET_IFINDEX);
            when(portMock.getToolTipText()).thenReturn(TARGET_TOOLTIP);
            when(vertexPortMock.getId()).thenReturn(TARGET_VERTEX_ID);
        }
    }

    public static void addSegment(boolean isSource, OnmsTopologyEdge edge) {
        OnmsTopologyPort segmentMock = mock(OnmsTopologyPort.class);

        OnmsTopologyVertex vertexSegmentMock = mock(OnmsTopologyVertex.class);
        when(segmentMock.getVertex()).thenReturn(vertexSegmentMock);

        if (isSource) {
            when(edge.getSource()).thenReturn(segmentMock);
            when(edge.getSource().getVertex().getNodeid()).thenReturn(null);
            when(segmentMock.getId()).thenReturn(SOURCE_ID);
            when(segmentMock.getToolTipText()).thenReturn(SOURCE_TOOLTIP);
        } else {
            when(edge.getTarget()).thenReturn(segmentMock);
            when(edge.getTarget().getVertex().getNodeid()).thenReturn(null);
            when(segmentMock.getId()).thenReturn(TARGET_ID);
            when(segmentMock.getToolTipText()).thenReturn(TARGET_TOOLTIP);
        }
    }

    public static void addNode(boolean isSource, OnmsTopologyEdge edge) {
        OnmsTopologyPort nodeMock = mock(OnmsTopologyPort.class);

        OnmsTopologyVertex vertexNodeMock = mock(OnmsTopologyVertex.class);
        when(nodeMock.getVertex()).thenReturn(vertexNodeMock);

        if (isSource) {
            when(edge.getSource()).thenReturn(nodeMock);
            when(edge.getSource().getIfindex()).thenReturn(null);
            when(nodeMock.getId()).thenReturn(SOURCE_ID);
            when(vertexNodeMock.getNodeid()).thenReturn(SOURCE_NODE_ID);
        } else {
            when(edge.getTarget()).thenReturn(nodeMock);
            when(edge.getTarget().getIfindex()).thenReturn(null);
            when(nodeMock.getId()).thenReturn(TARGET_ID);
            when(vertexNodeMock.getNodeid()).thenReturn(TARGET_NODE_ID);
        }
    }
}
