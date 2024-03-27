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
