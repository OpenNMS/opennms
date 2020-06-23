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

package org.opennms.features.apilayer.topology;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.apilayer.utils.EdgeMapper;
import org.opennms.features.apilayer.utils.NodeCriteriaCache;
import org.opennms.integration.api.v1.dao.EdgeDao;
import org.opennms.integration.api.v1.model.TopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class EdgeDaoImplTest {
    private final OnmsTopologyDao onmsTopologyDao = mock(OnmsTopologyDao.class);
    private final NodeCriteriaCache mockNodeCriteriaCache = mock(NodeCriteriaCache.class);
    private final EdgeDao edgeDao = new EdgeDaoImpl(onmsTopologyDao, new EdgeMapper(mockNodeCriteriaCache));
    private static final String CDP_EDGE_ID = "cdp.edge.id";
    private static final String ISIS_EDGE_ID = "isis.edge.id";

    @Before
    public void setup() {
        when(mockNodeCriteriaCache.getNodeCriteria(anyLong())).thenReturn(Optional.empty());
        Map<OnmsTopologyProtocol, OnmsTopology> topologyMap = new HashMap<>();

        when(onmsTopologyDao.getSupportedProtocols()).thenReturn(
                new HashSet<>(Arrays.asList(OnmsTopologyProtocol.create("CDP"), OnmsTopologyProtocol.create("ISIS"))));

        // Create two dummy edges to respond with for calls to the DAO
        OnmsTopology mockCDPTopology = mock(OnmsTopology.class);
        Set<OnmsTopologyEdge> cdpEdges = new HashSet<>();
        OnmsTopologyEdge mockCdpEdge = mock(OnmsTopologyEdge.class);
        when(mockCdpEdge.getId()).thenReturn(CDP_EDGE_ID);
        OnmsTopologyPort mockCdpEdgePort = mock(OnmsTopologyPort.class);
        OnmsTopologyVertex mockCdpEdgePortVertex = mock(OnmsTopologyVertex.class);
        when(mockCdpEdgePortVertex.getNodeid()).thenReturn(1);
        when(mockCdpEdgePort.getId()).thenReturn(CDP_EDGE_ID);
        when(mockCdpEdgePort.getVertex()).thenReturn(mockCdpEdgePortVertex);
        when(mockCdpEdge.getSource()).thenReturn(mockCdpEdgePort);
        when(mockCdpEdge.getTarget()).thenReturn(mockCdpEdgePort);
        cdpEdges.add(mockCdpEdge);
        when(mockCDPTopology.getEdges()).thenReturn(cdpEdges);
        topologyMap.put(OnmsTopologyProtocol.create("CDP"), mockCDPTopology);

        OnmsTopology mockIsIsTopology = mock(OnmsTopology.class);
        Set<OnmsTopologyEdge> isisEdges = new HashSet<>();
        OnmsTopologyEdge mockIsIsEdge = mock(OnmsTopologyEdge.class);
        when(mockIsIsEdge.getId()).thenReturn(ISIS_EDGE_ID);
        OnmsTopologyPort mockIsIsEdgePort = mock(OnmsTopologyPort.class);
        OnmsTopologyVertex mockIsIsEdgePortVertex = mock(OnmsTopologyVertex.class);
        when(mockIsIsEdgePortVertex.getNodeid()).thenReturn(2);
        when(mockIsIsEdgePort.getId()).thenReturn(ISIS_EDGE_ID);
        when(mockIsIsEdgePort.getVertex()).thenReturn(mockIsIsEdgePortVertex);
        when(mockIsIsEdge.getSource()).thenReturn(mockIsIsEdgePort);
        when(mockIsIsEdge.getTarget()).thenReturn(mockIsIsEdgePort);
        isisEdges.add(mockIsIsEdge);
        when(mockIsIsTopology.getEdges()).thenReturn(isisEdges);
        topologyMap.put(OnmsTopologyProtocol.create("ISIS"), mockIsIsTopology);

        when(onmsTopologyDao.getTopologies()).thenReturn(topologyMap);
    }

    @Test
    public void canCountEdges() {
        assertThat(edgeDao.getEdgeCount(), equalTo(2L));
        assertThat(edgeDao.getEdgeCount(TopologyProtocol.CDP), equalTo(1L));
        assertThat(edgeDao.getEdgeCount(TopologyProtocol.ISIS), equalTo(1L));
        assertThat(edgeDao.getEdgeCount(TopologyProtocol.BRIDGE), equalTo(0L));
    }

    @Test
    public void canGetEdges() {
        assertThat(edgeDao.getEdges().size(), equalTo(2));

        assertThat(edgeDao.getEdges(TopologyProtocol.CDP).size(), equalTo(1));
        assertThat(edgeDao.getEdges(TopologyProtocol.CDP).iterator().next().getId(), equalTo(CDP_EDGE_ID));

        assertThat(edgeDao.getEdges(TopologyProtocol.ISIS).size(), equalTo(1));
        assertThat(edgeDao.getEdges(TopologyProtocol.ISIS).iterator().next().getId(), equalTo(ISIS_EDGE_ID));

        assertThat(edgeDao.getEdges(TopologyProtocol.BRIDGE).size(), equalTo(0));
    }

    @Test
    public void canGetProtocols() {
        assertThat(edgeDao.getProtocols().size(), equalTo(2));
        assertThat(edgeDao.getProtocols(), hasItems(TopologyProtocol.CDP, TopologyProtocol.ISIS));
    }
}
