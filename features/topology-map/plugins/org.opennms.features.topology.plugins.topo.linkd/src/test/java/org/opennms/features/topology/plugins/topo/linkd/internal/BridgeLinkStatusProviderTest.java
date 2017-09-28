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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;

public class BridgeLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;

    @Before
    public void setUp(){
        m_node1 = new OnmsNode();
        m_node1.setId(1);

        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_bridgeMacLinkDao = EasyMock.createMock(BridgeMacLinkDao.class);
        m_bridgeBridgeLinkDao = EasyMock.createMock(BridgeBridgeLinkDao.class);

        m_statusProvider = new BridgeLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setBridgeMacLinkDao(m_bridgeMacLinkDao);
        m_statusProvider.setBridgeBridgeLinkDao(m_bridgeBridgeLinkDao);

        m_node2 = new OnmsNode();
        m_node2.setId(2);

        m_node3 = new OnmsNode();
        m_node3.setId(3);

    }

    @Test
    public void testGetLinkIds(){
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()).andReturn(new ArrayList<BridgeMacTopologyLink>());
        EasyMock.expect(m_bridgeBridgeLinkDao.findAll()).andReturn(new ArrayList<BridgeBridgeLink>());
        EasyMock.replay(m_bridgeMacLinkDao,m_bridgeBridgeLinkDao);

        Set<Integer> ids = m_statusProvider.getLinkIds(mapRefs(createEdges()));
        assertEquals(2, ids.size());
        Iterator<Integer> id=ids.iterator();
        assertEquals(521, id.next().intValue());
        assertEquals(522, id.next().intValue());

    }


    @Test
    public void testGetBridgeLinkStatus() {
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm()).anyTimes();
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks()).anyTimes();
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()).andReturn(new ArrayList<BridgeMacTopologyLink>()).anyTimes();
        EasyMock.expect(m_bridgeBridgeLinkDao.findAll()).andReturn(new ArrayList<BridgeBridgeLink>()).anyTimes();

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao,m_bridgeBridgeLinkDao);
        
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(3, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("up", status.computeStatus());
        }

        Map<EdgeRef, Status> statusMap2 = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(3, statusMap2.size());
        for (Status status : statusMap2.values()) {
            assertEquals("up", status.computeStatus());
        }

    }

    @Test
    public void testGetBridgeLinkStatusOneDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks()).anyTimes();
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()).andReturn(new ArrayList<BridgeMacTopologyLink>()).anyTimes();
        EasyMock.expect(m_bridgeBridgeLinkDao.findAll()).andReturn(new ArrayList<BridgeBridgeLink>()).anyTimes();

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao,m_bridgeBridgeLinkDao);
        
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(3, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));

        assertEquals(statusMap.get(edges.get(0)).computeStatus(), "up");
        assertEquals(statusMap.get(edges.get(1)).computeStatus(), "down");
        assertEquals(statusMap.get(edges.get(2)).computeStatus(), "up");
    }

    @Test
    public void testGetBridgeLinkStatusCloudDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createCloudDownAlarm());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()).andReturn(createBridgeLinks());
        EasyMock.expect(m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()).andReturn(new ArrayList<BridgeMacTopologyLink>());
        EasyMock.expect(m_bridgeBridgeLinkDao.findAll()).andReturn(new ArrayList<BridgeBridgeLink>());

        EasyMock.replay(m_alarmDao, m_bridgeMacLinkDao,m_bridgeBridgeLinkDao);
        List<EdgeRef> edges = createEdges();

        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);
        assertEquals(3, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        for (Status status : statusMap.values()){
            assertEquals("down", status.computeStatus());
        }
    }

    protected Map<String, EdgeRef> mapRefs(Collection<EdgeRef> edges) {
        Map<String, EdgeRef> retVal = new HashMap<String, EdgeRef>();
        for (EdgeRef edge : edges) {
            String nameSpace = EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE;
            if (edge.getNamespace().equals(nameSpace)) retVal.put(edge.getId(), edge);
        }
        return retVal;
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex cloudVertex = new AbstractVertex("nodes", "1:48", "cloud");
        Vertex targetVertex1 = new AbstractVertex("nodes", "2", "target1");
        Vertex targetVertex2 = new AbstractVertex("nodes", "3", "target2");

        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "521|521", sourceVertex, cloudVertex);
        EdgeRef edge1 = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "521|101", cloudVertex, targetVertex1);
        EdgeRef edge2 = new AbstractEdge(EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE, "522|102", cloudVertex, targetVertex2);

        return Arrays.asList(edge, edge1,edge2);
    }

    private List<BridgeMacTopologyLink> createBridgeLinks(){
        // now the query changed so no null nodeid is allowed
        BridgeMacTopologyLink link1 = new BridgeMacTopologyLink(521, 1, "mrkitty", ".1.3.6.1.4.2.9.9.1540", "Server Room", NodeType.ACTIVE, 48, 48, "port 48", null, 2, "mrmakay",null,null, NodeType.ACTIVE, "a8d0e5a0a490",24,"192.168.0.1",null,101, new Date());
        BridgeMacTopologyLink link2 = new BridgeMacTopologyLink(522, 1, "mrkitty", ".1.3.6.1.4.2.9.9.1540", "Server Room", NodeType.ACTIVE, 48, 48, "port 48", null, 3, "mrrusso",null,null, NodeType.ACTIVE, "a8d0e5a0a467",null,"192.168.0.2",null,102,new Date());
  
        return Arrays.asList(link1,link2);
    }

    private List<OnmsAlarm> createAlarm() {
        return Collections.emptyList();
    }

    private List<OnmsAlarm> createDownAlarm(){

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setNode(m_node2);
        alarm.setIfIndex(24);
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
