/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;

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
    public void testGetLinkIds(){
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());
        EasyMock.replay(m_bridgeMacLinkDao);

        Set<Integer> ids = m_statusProvider.getLinkIds(mapRefs(createEdges()));
        assertEquals(1, ids.size());

    }


    @Test
    public void testGetBridgeLinkStatus() {
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm()).anyTimes();
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao);
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(2, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("up", status.computeStatus());
        }

        BridgeMacLinkDao bridgeMacLinkDao = EasyMock.createMock(BridgeMacLinkDao.class);
        EasyMock.expect(bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());
        EasyMock.replay(bridgeMacLinkDao);

        m_statusProvider.setBridgeMacLinkDao(bridgeMacLinkDao);

        Map<EdgeRef, Status> statusMap2 = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(2, statusMap2.size());
        for (Status status : statusMap2.values()) {
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
    public void testGetBridgeLinkStatusOneLink(){
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

    protected Map<String, EdgeRef> mapRefs(Collection<EdgeRef> edges) {
        Map<String, EdgeRef> retVal = new HashMap<String, EdgeRef>();
        for (EdgeRef edge : edges) {
            String nameSpace = EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE;;
            if (edge.getNamespace().equals(nameSpace)) retVal.put(edge.getId(), edge);
        }
        return retVal;
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex cloudVertex = new AbstractVertex("nodes", "1|48", "cloud");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");

        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "1|48", sourceVertex, cloudVertex);
        EdgeRef edge2 = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "1|2", cloudVertex, targetVertex);

        return Arrays.asList(edge, edge2);
    }

    private List<BridgeMacTopologyLink> createBridgeLinks(){
        /*
        519 |      1 |         48 |                48 |                  |      | 00e08155403b | 172.20.1.16   |            |              |           |           548
        652 |      1 |         48 |                48 |                  |      | 00163e62e1c8 | 172.20.1.40   |            |              |           |           548
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 172.20.2.1    |            |              |           |           551
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 98.101.157.50 |            |              |           |           549
        521 |      1 |         48 |                48 |                  |      | a8d0e5a0a490 | 172.20.1.1    | 172.20.1.1 |            2 | mrmakay   |           548
        522 |      1 |         48 |                48 |                  |      | 000bdba886f4 | 172.20.1.19   |            |              |           |           548
        523 |      1 |         48 |                48 |                  |      | 00163e7efe74 | 172.20.1.32   |            |              |           |           548
        524 |      1 |         48 |                48 |                  |      | a8d0e5a0a488 |               |            |              |           |
        525 |      1 |         48 |                48 |                  |      | 782bcb446c68 |               |            |              |           |
        604 |      1 |         48 |                48 |                  |      | 782bcb446c66 | 172.20.1.11   |            |              |           |           548
        527 |      1 |         48 |                48 |                  |      | b8ca3aeb75a8 | 172.20.1.254  |            |              |           |           548
        528 |      1 |         48 |                48 |                  |      | 14feb5cf15b9 | 172.20.1.23   |            |              |           |           548
        518 |      1 |         48 |                48 |                  |      | 14feb5cf15c1 |               |            |              |           |
        517 |      1 |         48 |                48 |                  |      | 000d566ffea8 | 172.20.1.39   |            |              |           |           548
        516 |      1 |         48 |                48 |                  |      | 525400eeb91d | 172.20.1.41   |            |              |           |           548
        515 |      1 |         48 |                48 |                  |      | 00163e1d0215 | 172.20.1.24   |            |              |           |           548
        514 |      1 |         48 |                48 |                  |      | 00163e69ab49 | 172.20.1.38   |            |              |           |           548
 */
        BridgeMacTopologyLink link1 = new BridgeMacTopologyLink(519, 1, 48, 48, null, null, "00e08155403b", "172.20.1.16", null, null, null, 548);
        BridgeMacTopologyLink link2 = new BridgeMacTopologyLink(652, 1, 48, 48, null, null, "00163e62e1c8", "172.20.1.40", null, null, null, 548);
        BridgeMacTopologyLink link3 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "172.20.2.1", null, null, null, 551);
        BridgeMacTopologyLink link4 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "98.101.157.50", null, null, null, 549);
        BridgeMacTopologyLink link5 = new BridgeMacTopologyLink(521, 1, 48, 48, null, null, "a8d0e5a0a490", "172.20.1.1", "172.20.1.1", 2, "mrmakay", 548);

        BridgeMacTopologyLink link6 = new BridgeMacTopologyLink(522, 1, 48, 48, null, null, "000bdba886f4", "172.20.1.19", null, null, null, 548);
        BridgeMacTopologyLink link7 = new BridgeMacTopologyLink(523, 1, 48, 48, null, null, "00163e7efe74", "172.20.1.24", null, null, null, 548);
        BridgeMacTopologyLink link8 = new BridgeMacTopologyLink(524, 1, 48, 48, null, null, "a8d0e5a0a488", null, null, null, null, 548);

        return Arrays.asList(link1, link2, link3, link4, link5, link6, link7, link8);
    }

    private List<OnmsAlarm> createAlarm() {
        return Collections.emptyList();
    }

    private List<OnmsAlarm> createDownAlarm(){

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node2);
        alarm.setIfIndex(548);
        alarm.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);

        return Arrays.asList(alarm);
    }

    private List<OnmsAlarm> createCloudDownAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node1);
        alarm.setIfIndex(48);
        alarm.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);

        return Arrays.asList(alarm);
    }
}
