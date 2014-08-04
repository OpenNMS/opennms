/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class BridgeLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;

    @Before
    public void setUp(){
        m_node1 = new OnmsNode();
        m_node1.setId(1);

        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_bridgeMacLinkDao = EasyMock.createMock(BridgeMacLinkDao.class);

        m_statusProvider = new BridgeLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setBridgeMacLinkDao(m_bridgeMacLinkDao);

        m_node2 = new OnmsNode();
        m_node2.setId(2);
    }

    @Test
    public void testGetBridgeLinkStatus() {
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao);
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(2, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("up", status.computeStatus());
        }

    }

    @Test
    public void testGetBridgeLinkStatusOneDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao);
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(2, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));

        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "down");
    }

    @Test
    public void testGetBridgeLinkStatusCloudDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createCloudDownAlarm());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao);
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(2, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("down", status.computeStatus());
        }
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex cloudVertex = new AbstractVertex("nodes", "1|48", "cloud");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");

        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "1|48", sourceVertex, cloudVertex);
        EdgeRef edge2 = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "1|2", cloudVertex, targetVertex);

        return Arrays.asList(edge, edge2);
    }

    private List<BridgeMacTopologyLink> createBridgeLinks() {

        /*
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 172.20.2.1    | 172.20.2.1    |            2 | mrmakay   |           551
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 98.101.157.50 | 98.101.157.50 |            2 | mrmakay   |           549
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 172.20.1.1    | 172.20.1.1    |            2 | mrmakay   |           548
         */

        BridgeMacTopologyLink link1 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "172.20.2.1", "172.20.2.1", 2, "mrmakay", 551);
        BridgeMacTopologyLink link2 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "98.101.157.50", "98.101.157.50", 2, "mrmakay", 549);
        BridgeMacTopologyLink link3 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "172.20.1.1", "172.20.1.1", 2, "mrmakay", 548);

        List<BridgeMacTopologyLink> links = new ArrayList<BridgeMacTopologyLink>();
        links.add(link1);
        links.add(link2);
        links.add(link3);

        return links;
    }

    private List<OnmsAlarm> createAlarm() {
        return Collections.EMPTY_LIST;
    }

    private List<OnmsAlarm> createDownAlarm(){

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node2);
        alarm.setIfIndex(548);
        alarm.setUei("uei.opennms.org/internal/topology/linkDown");

        return Arrays.asList(alarm);
    }

    private List<OnmsAlarm> createCloudDownAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node1);
        alarm.setIfIndex(48);
        alarm.setUei("uei.opennms.org/internal/topology/linkDown");

        return Arrays.asList(alarm);
    }
}
