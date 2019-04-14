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
import static org.opennms.topologies.service.api.EdgeMockUtil.PROTOCOL;
import static org.opennms.topologies.service.api.EdgeMockUtil.SOURCE_ID;
import static org.opennms.topologies.service.api.EdgeMockUtil.TARGET_NODE_ID;
import static org.opennms.topologies.service.api.EdgeMockUtil.addPort;
import static org.opennms.topologies.service.api.EdgeMockUtil.createEdge;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.integration.api.v1.model.TopologyPort;
import org.opennms.integration.api.v1.model.TopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.topologies.service.api.EdgeMockUtil;

public class ModelMappersTest {
    private final NodeCriteriaCache nodeCriteriaCache = mock(NodeCriteriaCache.class);
    private final EdgeMapper edgeMapper = new EdgeMapper(nodeCriteriaCache);

    @Before
    public void setup() {
        when(nodeCriteriaCache.getNodeCriteria(Matchers.any(Long.class))).thenReturn(Optional.empty());
    }

    @Test
    public void canMapNodeToNodeEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addNode(true, mockEdge);
        EdgeMockUtil.addNode(false, mockEdge);
        TopologyEdge mappedEdge = edgeMapper.toEdge(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getProtocol().name(), equalTo(PROTOCOL));

        AtomicBoolean visitedSourceNode = new AtomicBoolean(false);
        AtomicBoolean visitedTargetNode = new AtomicBoolean(false);
        mappedEdge.visitEndpoints(new TopologyEdge.EndpointVisitor() {
            @Override
            public void visitSource(Node node) {
                assertThat(node.getId(), equalTo(EdgeMockUtil.SOURCE_NODE_ID));
                visitedSourceNode.set(true);
            }

            @Override
            public void visitTarget(Node node) {
                assertThat(node.getId(), equalTo(TARGET_NODE_ID));
                visitedTargetNode.set(true);
            }
        });
        assertThat(visitedSourceNode.get(), equalTo(true));
        assertThat(visitedTargetNode.get(), equalTo(true));
    }

    @Test
    public void canMapPortToPortEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        addPort(true, mockEdge);
        addPort(false, mockEdge);
        TopologyEdge mappedEdge = edgeMapper.toEdge(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getProtocol().name(), equalTo(PROTOCOL));

        AtomicBoolean visitedSourcePort = new AtomicBoolean(false);
        AtomicBoolean visitedTargetPort = new AtomicBoolean(false);
        mappedEdge.visitEndpoints(new TopologyEdge.EndpointVisitor() {
            @Override
            public void visitSource(TopologyPort port) {
                assertThat(port.getId(), equalTo(SOURCE_ID));
                assertThat(port.getIfIndex(), equalTo(EdgeMockUtil.SOURCE_IFINDEX));
                assertThat(port.getTooltipText(), equalTo(EdgeMockUtil.SOURCE_TOOLTIP));
                assertThat(port.getNodeCriteria().getId(), equalTo(EdgeMockUtil.SOURCE_NODE_ID));
                visitedSourcePort.set(true);
            }

            @Override
            public void visitTarget(TopologyPort port) {
                assertThat(port.getId(), equalTo(EdgeMockUtil.TARGET_ID));
                assertThat(port.getIfIndex(), equalTo(EdgeMockUtil.TARGET_IFINDEX));
                assertThat(port.getTooltipText(), equalTo(EdgeMockUtil.TARGET_TOOLTIP));
                assertThat(port.getNodeCriteria().getId(), equalTo(TARGET_NODE_ID));
                visitedTargetPort.set(true);
            }
        });
        assertThat(visitedSourcePort.get(), equalTo(true));
        assertThat(visitedTargetPort.get(), equalTo(true));
    }

    @Test
    public void canMapSegmentToSegmentEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addSegment(true, mockEdge);
        EdgeMockUtil.addSegment(false, mockEdge);
        TopologyEdge mappedEdge = edgeMapper.toEdge(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getProtocol().name(), equalTo(PROTOCOL));

        AtomicBoolean visitedSourceSegment = new AtomicBoolean(false);
        AtomicBoolean visitedTargetSegment = new AtomicBoolean(false);
        mappedEdge.visitEndpoints(new TopologyEdge.EndpointVisitor() {
            @Override
            public void visitSource(TopologySegment segment) {
                assertThat(segment.getId(), equalTo(SOURCE_ID));
                assertThat(segment.getTooltipText(), equalTo(EdgeMockUtil.SOURCE_TOOLTIP));
                assertThat(segment.getSegmentCriteria(), equalTo(String.format("%s:%s", PROTOCOL, SOURCE_ID)));
                visitedSourceSegment.set(true);
            }

            @Override
            public void visitTarget(TopologySegment segment) {
                assertThat(segment.getId(), equalTo(EdgeMockUtil.TARGET_ID));
                assertThat(segment.getTooltipText(), equalTo(EdgeMockUtil.TARGET_TOOLTIP));
                assertThat(segment.getSegmentCriteria(), equalTo(String.format("%s:%s", PROTOCOL,
                        EdgeMockUtil.TARGET_ID)));
                visitedTargetSegment.set(true);
            }
        });
        assertThat(visitedSourceSegment.get(), equalTo(true));
        assertThat(visitedTargetSegment.get(), equalTo(true));
    }

    @Test
    public void canMapPortToSegmentEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        addPort(true, mockEdge);
        EdgeMockUtil.addSegment(false, mockEdge);
        TopologyEdge mappedEdge = edgeMapper.toEdge(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getProtocol().name(), equalTo(PROTOCOL));

        AtomicBoolean visitedSourcePort = new AtomicBoolean(false);
        AtomicBoolean visitedTargetSegment = new AtomicBoolean(false);
        mappedEdge.visitEndpoints(new TopologyEdge.EndpointVisitor() {
            @Override
            public void visitSource(TopologyPort port) {
                assertThat(port.getId(), equalTo(SOURCE_ID));
                assertThat(port.getIfIndex(), equalTo(EdgeMockUtil.SOURCE_IFINDEX));
                assertThat(port.getTooltipText(), equalTo(EdgeMockUtil.SOURCE_TOOLTIP));
                assertThat(port.getNodeCriteria().getId(), equalTo(EdgeMockUtil.SOURCE_NODE_ID));
                visitedSourcePort.set(true);
            }

            @Override
            public void visitTarget(TopologySegment segment) {
                assertThat(segment.getId(), equalTo(EdgeMockUtil.TARGET_ID));
                assertThat(segment.getTooltipText(), equalTo(EdgeMockUtil.TARGET_TOOLTIP));
                assertThat(segment.getSegmentCriteria(), equalTo(String.format("%s:%s", PROTOCOL,
                        EdgeMockUtil.TARGET_ID)));
                visitedTargetSegment.set(true);
            }
        });
        assertThat(visitedSourcePort.get(), equalTo(true));
        assertThat(visitedTargetSegment.get(), equalTo(true));
    }
}
