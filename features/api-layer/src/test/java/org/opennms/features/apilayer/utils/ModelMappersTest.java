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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.integration.api.v1.model.TopologyPort;
import org.opennms.integration.api.v1.model.TopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class ModelMappersTest {
    @Test
    public void canMapEdge() {
        String id = "id";
        String tooltipText = "tooltip";

        OnmsTopologyEdge sourceEdgeMock = mock(OnmsTopologyEdge.class);
        when(sourceEdgeMock.getId()).thenReturn(id);
        when(sourceEdgeMock.getToolTipText()).thenReturn(tooltipText);

        // Source port
        String sourceId = "sourceId";
        String sourceTooltipText = "sourceTooltip";
        Integer sourceIfIndex = 1;
        Integer sourceVertexNodeId = 1;
        OnmsTopologyVertex sourceVertexMock = mock(OnmsTopologyVertex.class);
        when(sourceVertexMock.getNodeid()).thenReturn(sourceVertexNodeId);

        OnmsTopologyPort sourcePortMock = mock(OnmsTopologyPort.class);
        when(sourcePortMock.getId()).thenReturn(sourceId);
        when(sourcePortMock.getToolTipText()).thenReturn(sourceTooltipText);
        when(sourcePortMock.getIfindex()).thenReturn(sourceIfIndex);
        when(sourcePortMock.getVertex()).thenReturn(sourceVertexMock);

        // Target port
        String targetId = "targetId";
        String targetTooltipText = "targetTooltip";
        Integer targetIfIndex = 2;
        Integer targetVertexNodeId = 2;
        String targetVertexId = "vertex-id";
        OnmsTopologyVertex targetVertexMock = mock(OnmsTopologyVertex.class);
        when(targetVertexMock.getId()).thenReturn(targetVertexId);
        when(targetVertexMock.getNodeid()).thenReturn(targetVertexNodeId);

        OnmsTopologyPort targetPortMock = mock(OnmsTopologyPort.class);
        when(targetPortMock.getId()).thenReturn(targetId);
        when(targetPortMock.getToolTipText()).thenReturn(targetTooltipText);
        when(targetPortMock.getIfindex()).thenReturn(targetIfIndex);
        when(targetPortMock.getVertex()).thenReturn(targetVertexMock);

        when(sourceEdgeMock.getSource()).thenReturn(sourcePortMock);
        when(sourceEdgeMock.getTarget()).thenReturn(targetPortMock);

        // Map when the target is a port
        String protocol = "test-protocol";
        TopologyEdge mappedEdge = ModelMappers.toEdge(OnmsTopologyProtocol.create(protocol), sourceEdgeMock);

        assertThat(mappedEdge.getId(), equalTo(id));
        assertThat(mappedEdge.getProtocol(), equalTo(protocol));
        assertThat(mappedEdge.getTooltipText(), equalTo(tooltipText));
        assertThat(mappedEdge.getSource().getId(), equalTo(sourceId));
        assertThat(mappedEdge.getSource().getIfIndex(), equalTo(sourceIfIndex));
        assertThat(mappedEdge.getSource().getTooltipText(), equalTo(sourceTooltipText));
        assertThat(mappedEdge.getSource().getNodeCriteria().getId(), equalTo(sourceVertexNodeId));

        mappedEdge.visitTarget(new TopologyEdge.TopologyEdgeTargetVisitor() {
            @Override
            public void visitTargetPort(TopologyPort port) {
                assertThat(port.getId(), equalTo(targetId));
                assertThat(port.getIfIndex(), equalTo(targetIfIndex));
                assertThat(port.getTooltipText(), equalTo(targetTooltipText));
                assertThat(port.getNodeCriteria().getId(), equalTo(targetVertexNodeId));
            }
        });

        // Map when the target is a segment
        when(targetVertexMock.getId()).thenReturn(null);
        mappedEdge = ModelMappers.toEdge(OnmsTopologyProtocol.create("test-protocol"), sourceEdgeMock);

        mappedEdge.visitTarget(new TopologyEdge.TopologyEdgeTargetVisitor() {
            @Override
            public void visitTargetSegement(TopologySegment segment) {
                assertThat(segment.getId(), equalTo(targetVertexId));
                assertThat(segment.getTooltipText(), equalTo(targetTooltipText));
                assertThat(segment.getSegmentCriteria(), equalTo(String.format("%s:%s", protocol, targetId)));
            }
        });
    }
}
