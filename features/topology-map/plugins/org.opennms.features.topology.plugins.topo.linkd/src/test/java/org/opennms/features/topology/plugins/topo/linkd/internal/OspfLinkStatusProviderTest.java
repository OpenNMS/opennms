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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;

public class OspfLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private OspfLinkDao m_ospfLinkDao;
    private OspfLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_nodeChennai;
    private OnmsNode m_nodeDehli;

    @Before
    public void setUp() {
        m_node1 = new OnmsNode();
        m_node1.setId(1);

        m_node2 = new OnmsNode();
        m_node2.setId(2);

        m_nodeChennai = new OnmsNode();
        m_nodeChennai.setId(14);

        m_nodeDehli = new OnmsNode();
        m_nodeDehli.setId(10);


        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_ospfLinkDao = EasyMock.createMock(OspfLinkDao.class);

        m_statusProvider = new OspfLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setOspfLinkDao(m_ospfLinkDao);

        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);

    }

    @Test
    public void testGetOspfLinkStatus() {

        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createEmptyAlarmList());
        EasyMock.expect(m_ospfLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createOspfLinks());

        EasyMock.replay(m_alarmDao, m_ospfLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("up", status.computeStatus());

    }

    @Test
    public void testGetOspfLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_ospfLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createOspfLinks());

        EasyMock.replay(m_alarmDao, m_ospfLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("down", status.computeStatus());
    }

    @Test
    public void testSPC944OspfLinkStatus(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createEmptyAlarmList());
        EasyMock.expect(m_ospfLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createChennaiDehliLinks());

        EasyMock.replay(m_alarmDao, m_ospfLinkDao);

        List<EdgeRef> edges = createChennaiToDehli();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("up", status.computeStatus());
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");
        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "1|2", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }


    private List<EdgeRef> createChennaiToDehli() {
        Vertex sourceVertex = new AbstractVertex("nodes", "14", "CHENNAI");
        Vertex targetVertex = new AbstractVertex("nodes", "10", "DEHLI");
        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "7|8", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }

    private List<OspfLink> createChennaiDehliLinks(){
        List<OspfLink> links = new ArrayList<OspfLink>();

        OspfLink link = createOspfLink(m_nodeChennai, "10.205.56.21", "255.255.0.0", 0, 13, "192.168.8.1", "10.205.56.8", 0);
        link.setId(7);
        links.add(link);

        OspfLink link2 = createOspfLink(m_nodeDehli, "10.205.56.8", "255.255.0.0", 0, 13, "192.168.6.1", "10.205.56.21", 0);
        link2.setId(8);
        links.add(link2);

        return links;
    }

    private List<OspfLink> createOspfLinks() {
        List<OspfLink> links = new ArrayList<OspfLink>();

        OspfLink link = createOspfLink(m_node1, "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
        link.setId(1);
        links.add(link);

        OspfLink link2 = createOspfLink(m_node2, "192.168.100.245", "255.255.255.252", 0, 10100, "192.168.100.250", "192.168.100.246", 0);
        link2.setId(2);
        links.add(link2);

        return links;
    }

    private List<OnmsAlarm> createEmptyAlarmList() {
        return Collections.emptyList();
    }

    private List<OnmsAlarm> createDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<OnmsAlarm>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node1);
        alarm1.setIfIndex(10101);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private OspfLink createOspfLink(OnmsNode node, String sourceIpAddr, String sourceIpMask, int addrLessIndex, int ifIndex, String remRouterId, String remIpAddr, int remAddrLessIndex) {
        final OspfLink ospfLink = new OspfLink();
        ospfLink.setNode(node);
        ospfLink.setOspfIpAddr(InetAddressUtils.addr(sourceIpAddr));
        ospfLink.setOspfIpMask(InetAddressUtils.addr(sourceIpMask));
        ospfLink.setOspfAddressLessIndex(addrLessIndex);
        ospfLink.setOspfIfIndex(ifIndex);
        ospfLink.setOspfRemRouterId(InetAddressUtils.addr(remRouterId));
        ospfLink.setOspfRemIpAddr(InetAddressUtils.addr(remIpAddr));
        ospfLink.setOspfRemAddressLessIndex(remAddrLessIndex);
        return ospfLink;
    }

}
