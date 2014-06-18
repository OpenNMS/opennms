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
import org.easymock.internal.matchers.Any;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class OspfLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private OspfLinkDao m_lldpLinkDao;
    private OspfLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;

    @Before
    public void setUp() {
        m_node1 = new OnmsNode();
        m_node1.setId(1);
        m_node1.setLldpElement(new LldpElement(m_node1, "node1ChassisId", "node1SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

        m_node2 = new OnmsNode();
        m_node2.setId(2);
        m_node2.setLldpElement(new LldpElement(m_node2, "node2ChassisId", "node2SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_lldpLinkDao = EasyMock.createMock(OspfLinkDao.class);

        m_statusProvider = new OspfLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setOspfLinkDao(m_lldpLinkDao);

        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);

    }

    @Test
    public void testGetLldpLinkStatus() {

        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm());
        EasyMock.expect(m_lldpLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createOspfLinks());

        EasyMock.replay(m_alarmDao, m_lldpLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("up", status.computeStatus());

    }

    @Test
    public void testGetLldpLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_lldpLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createOspfLinks());

        EasyMock.replay(m_alarmDao, m_lldpLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("down", status.computeStatus());
    }

    private List<EdgeAlarmStatusSummary> createDownLldpStatusSummary() {
        EdgeAlarmStatusSummary summary = new EdgeAlarmStatusSummary(1,2, EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        return Arrays.asList(summary);
    }

    private List<EdgeAlarmStatusSummary> createLldpLinkStatusSummary() {
        EdgeAlarmStatusSummary summary = new EdgeAlarmStatusSummary(1,2, EventConstants.TOPOLOGY_LINK_UP_EVENT_UEI);
        return Arrays.asList(summary);
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");
        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "1|2", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }


    private List<OspfLink> createOspfLinks() {
        List<OspfLink> links = new ArrayList<OspfLink>();

        OspfLink link = createOspfLink(m_node1, "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
        link.setId(1);
        links.add(link);

        OspfLink link2 = createOspfLink(m_node2, "192.168.100.245", "255.255.255.252", 0, 10101, "192.168.100.250", "192.168.100.246", 0);
        link2.setId(2);
        links.add(link2);

        return links;
    }

    private List<OnmsAlarm> createAlarm() {
        return Collections.EMPTY_LIST;
    }

    private List<OnmsAlarm> createDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<OnmsAlarm>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node1);
        alarm1.setIfIndex(10101);
        alarm1.setUei("uei.opennms.org/internal/topology/linkDown");
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
